/*
 * Copyright (C) 2018 Frans Jacobs.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package lan.wervel.jcs.repository;

import lan.wervel.jcs.common.TrackRepository;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lan.wervel.jcs.common.ControllerProvider;
import lan.wervel.jcs.repository.model.AttributeChangeListener;
import lan.wervel.jcs.repository.model.AttributeChangedEvent;
import lan.wervel.jcs.server.rmi.ServerInfo;
import lan.wervel.jcs.server.rmi.ServerInfoProvider;

import org.pmw.tinylog.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import lan.wervel.jcs.repository.model.Crane;
import lan.wervel.jcs.repository.model.DriveWay;
import lan.wervel.jcs.repository.model.DriveWay.TrackStatus;
import lan.wervel.jcs.repository.model.FeedbackModule;
import lan.wervel.jcs.repository.model.Locomotive;
import lan.wervel.jcs.repository.model.Locomotive.Direction;
import lan.wervel.jcs.repository.model.SolenoidAccessoiry;
import lan.wervel.jcs.repository.model.SolenoidAccessoiry.StatusType;
import lan.wervel.jcs.repository.model.SolenoidAccessoiry.Type;
import lan.wervel.marklin.track.monitor.TrackEvent;
import lan.wervel.marklin.track.monitor.TrackEventListener;

public class XmlRepository implements TrackRepository, AttributeChangeListener {

  private static final String DEFAULT_FILENAME = "repository.xml";

  private final Map<Integer, SolenoidAccessoiry> solenoidAccessoiries;
  private final Map<Integer, Locomotive> locomotives;
  private final Map<Integer, Crane> cranes;
  private final Map<Integer, FeedbackModule> feedbackModules;
  private final Map<Integer, DriveWay> driveWays;

  private final List<ControllerProvider> controllers;
  private final List<TrackEventListener> trackEventListeners;

  private FeedbackTimerTask feedbackTimerTask;

  private static Preferences prefs;

  private boolean fbToggle = false;

  public XmlRepository() {
    this(DEFAULT_FILENAME);
  }

  public XmlRepository(String filename) {
    long start = System.currentTimeMillis();
    prefs = Preferences.userRoot().node(this.getClass().getName());

    solenoidAccessoiries = new HashMap<>();
    locomotives = new HashMap<>();
    cranes = new HashMap<>();
    feedbackModules = new HashMap<>();
    driveWays = new HashMap<>();
    controllers = new ArrayList<>();
    trackEventListeners = new ArrayList<>();

    Logger.trace("Start loading items from file: " + filename + "...");
    loadItems(filename);

    long end = System.currentTimeMillis();
    Logger.debug("Loaded all track objects. Duration " + (end - start) + " ms.");
  }

  private String getElementStringValue(Element element, String elementName) {
    String value = null;
    NodeList nodeList = element.getElementsByTagName(elementName);

    if (nodeList != null && nodeList.item(0) != null) {
      Element el = (Element) nodeList.item(0);
      NodeList textList = el.getChildNodes();
      if (textList.getLength() > 0) {
        value = ((Node) textList.item(0)).getNodeValue().trim();
      }
    }
    return value;
  }

  private Integer getElementIntegerValue(Element element, String elementName) {
    Integer value = null;
    NodeList nodeList = element.getElementsByTagName(elementName);
    Element el = (Element) nodeList.item(0);
    if (el != null && el.getChildNodes() != null) {
      NodeList textList = el.getChildNodes();
      if (textList.getLength() > 0) {
        value = Integer.parseInt(((Node) textList.item(0)).getNodeValue().trim());
      }
    }
    return value;
  }

  private void loadItems(String filename) {
    try {
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      File conf = new File(filename);
      Document doc;
      if (conf.exists()) {
        doc = docBuilder.parse(conf);
      } else {
        doc = docBuilder
                .parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(DEFAULT_FILENAME));
      }
      doc.getDocumentElement().normalize();

      NodeList locList = doc.getElementsByTagName("locomotive");
      loadLocs(locList);

      NodeList craneList = doc.getElementsByTagName("crane");
      loadCranes(craneList);

      NodeList turnoutList = doc.getElementsByTagName("turnout");
      loadTurnouts(turnoutList);

      NodeList listOfSignals = doc.getElementsByTagName("signal");
      loadSignals(listOfSignals);

      NodeList listOfFeedbackModules = doc.getElementsByTagName("feedbackModule");
      loadFeedbackModules(listOfFeedbackModules);

      NodeList listOfDriveWays = doc.getElementsByTagName("driveway");
      loadDriveWays(listOfDriveWays);

    } catch (SAXParseException err) {
      Logger.error("Parsing error, line " + err.getLineNumber() + ", uri " + err.getSystemId());
      Logger.error(err.getMessage());
    } catch (SAXException | ParserConfigurationException | IOException e) {
      Logger.error(e);
    }
  }

  private void loadCranes(NodeList craneList) {
    int cnt = craneList.getLength();
    Logger.trace("Loading " + cnt + " cranes...");

    for (int s = 0; s < craneList.getLength(); s++) {
      Node objectNode = craneList.item(s);
      if (objectNode.getNodeType() == Node.ELEMENT_NODE) {
        Element objectElement = (Element) objectNode;

        Integer address = getElementIntegerValue(objectElement, "address");
        String name = getElementStringValue(objectElement, "name");
        String description = getElementStringValue(objectElement, "description");
        String catalogNumber = getElementStringValue(objectElement, "catalognr");
        //String function = getElementStringValue(objectElement, "function");
        //String f1 = getElementStringValue(objectElement, "function1");
        //String f2 = getElementStringValue(objectElement, "function2");
        //String f3 = getElementStringValue(objectElement, "function3");
        //String f4 = getElementStringValue(objectElement, "function4");
        Integer minSpeed = getElementIntegerValue(objectElement, "minSpeed");
        Crane crane = new Crane(address, name, description, catalogNumber, minSpeed, false, false, false, false, false);
        cranes.put(crane.getAddress(), crane);
      }
    }
  }

  private void loadLocs(NodeList locList) {
    int cnt = locList.getLength();
    Logger.trace("Loading " + cnt + " locomotives...");

    for (int s = 0; s < locList.getLength(); s++) {
      Node objectNode = locList.item(s);
      if (objectNode.getNodeType() == Node.ELEMENT_NODE) {
        Element objectElement = (Element) objectNode;

        Integer address = getElementIntegerValue(objectElement, "address");
        String type = getElementStringValue(objectElement, "type");
        String name = getElementStringValue(objectElement, "name");
        String description = getElementStringValue(objectElement, "description");
        String catalogNumber = getElementStringValue(objectElement, "catalognr");

        String defaultDirection = getElementStringValue(objectElement, "defaultDirection");
        Integer speedSteps = getElementIntegerValue(objectElement, "speedSteps");
        Integer minSpeed = getElementIntegerValue(objectElement, "minSpeed");
        String specialFunctions = getElementStringValue(objectElement, "specialFunctions");

        if (address != null) {
          Locomotive loc = new Locomotive(address, name, description, catalogNumber, minSpeed, type, false, false, false, false, false);

          switch (defaultDirection) {
            case "Forwards":
              loc.setDefaultDirection(Locomotive.Direction.Forwards);
              break;
            case "Backwards":
              loc.setDefaultDirection(Locomotive.Direction.Backwards);
              break;
            default:
              loc.setDefaultDirection(Locomotive.Direction.Forwards);
              break;
          }

          loc.setDirection(loc.getDefaultDirection());
          loc.setSpeed(0);

          loc.setSpeedSteps(speedSteps);
          loc.setSpecialFuctions("Y".equalsIgnoreCase(specialFunctions));
          loc.setF0Type(getElementStringValue(objectElement, "function"));

          if (loc.isSpecialFuctions()) {
            loc.setF1Type(getElementStringValue(objectElement, "function1"));
            loc.setF2Type(getElementStringValue(objectElement, "function2"));
            loc.setF3Type(getElementStringValue(objectElement, "function3"));
            loc.setF4Type(getElementStringValue(objectElement, "function4"));
          }
          locomotives.put(loc.getAddress(), loc);
        }
      }
    }
  }

  private void loadTurnouts(NodeList turnoutList) {
    int cnt = turnoutList.getLength();
    Logger.trace("Loading " + cnt + " turnouts...");

    for (int s = 0; s < turnoutList.getLength(); s++) {
      Node objectNode = turnoutList.item(s);
      if (objectNode.getNodeType() == Node.ELEMENT_NODE & "turnouts".equals(objectNode.getParentNode().getNodeName())) {
        Element objectElement = (Element) objectNode;

        Integer address = getElementIntegerValue(objectElement, "address");
        String direction = getElementStringValue(objectElement, "direction");
        String catalogNumber = getElementStringValue(objectElement, "catalognr");

        SolenoidAccessoiry sa = new SolenoidAccessoiry(address, direction, catalogNumber, Type.TURNOUT);
        //Obtain the last used value from the prefs
        String value = prefs.get(SolenoidAccessoiry.class.getSimpleName() + "_" + sa.getType() + "_" + sa.getAddress(), "GREEN");
        sa.setStatus(value);
        this.solenoidAccessoiries.put(sa.getAddress(), sa);
      }
    }
  }

  private void loadSignals(NodeList signalList) {
    int cnt = signalList.getLength();
    Logger.trace("Loading " + cnt + " signals...");

    for (int s = 0; s < signalList.getLength(); s++) {
      Node objectNode = signalList.item(s);

      if (objectNode.getNodeType() == Node.ELEMENT_NODE && "signals".equals(objectNode.getParentNode().getNodeName())) {
        Element objectElement = (Element) objectNode;

        Integer address = getElementIntegerValue(objectElement, "address");
        String type = getElementStringValue(objectElement, "type");
        String catalogNumber = getElementStringValue(objectElement, "catalognr");

        SolenoidAccessoiry sa = new SolenoidAccessoiry(address, type, catalogNumber, Type.SIGNAL);
        //Obtain the last used value from the prefs
        String value = prefs.get(SolenoidAccessoiry.class.getSimpleName() + "_" + sa.getType() + "_" + sa.getAddress(), "GREEN");
        sa.setStatus(value);
        this.solenoidAccessoiries.put(sa.getAddress(), sa);
      }
    }
  }

  private void loadFeedbackModules(NodeList feedbackModuleList) {
    int cnt = feedbackModuleList.getLength();
    Logger.trace("Loading " + cnt + " feedback Modules...");

    for (int s = 0; s < feedbackModuleList.getLength(); s++) {
      Node objectNode = feedbackModuleList.item(s);
      if (objectNode.getNodeType() == Node.ELEMENT_NODE) {
        Element objectElement = (Element) objectNode;

        Integer moduleNumber = getElementIntegerValue(objectElement, "moduleNumber");
        Integer modulePorts = getElementIntegerValue(objectElement, "modulePorts");
        String catalogNumber = getElementStringValue(objectElement, "catalognr");

        FeedbackModule feedbackModule = new FeedbackModule(moduleNumber, catalogNumber, modulePorts);
        this.feedbackModules.put(feedbackModule.getModuleNumber(), feedbackModule);
      }
    }
  }

  private void loadDriveWays(NodeList nodeList) {
    int cnt = nodeList.getLength();
    Logger.trace("Loading " + cnt + " driveways...");

    for (int s = 0; s < nodeList.getLength(); s++) {
      Node objectNode = nodeList.item(s);
      if (objectNode.getNodeType() == Node.ELEMENT_NODE) {
        Element objectElement = (Element) objectNode;

        Integer address = getElementIntegerValue(objectElement, "address");
        String name = getElementStringValue(objectElement, "name");
        String description = getElementStringValue(objectElement, "description");
        String type = getElementStringValue(objectElement, "type");
        TrackStatus trackStatus = "route".equals(type) ? TrackStatus.ROUTING : TrackStatus.FREE;
        Map<Integer, String> dwm = new HashMap<>();
        Map<Integer, String> accessoiries = new LinkedHashMap<>();

        NodeList saList = objectElement.getElementsByTagName("solenoidaccessoiries");
        for (int i = 0; i < saList.getLength(); i++) {
          Node objectSubNode = saList.item(i);
          if (objectSubNode.hasChildNodes()) {
            NodeList childNodes = objectSubNode.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
              Node n = childNodes.item(j);
              Node ns = n.getNextSibling();
              if (ns != null) {
                if (ns.hasChildNodes() && ns.getNodeType() == Node.ELEMENT_NODE) {
                  Element e = (Element) ns;
                  Integer aa = getElementIntegerValue(e, "address");
                  String status = getElementStringValue(e, "status");
                  accessoiries.put(aa, status);
                }
              }
            }
          }
        }

        //Obtain the "real Solenoids", so ensure this is called after the loading of Signals and Turnouts
        Map<SolenoidAccessoiry, StatusType> sastm = new LinkedHashMap<>();

        for (Integer addrs : accessoiries.keySet()) {
          SolenoidAccessoiry sa = this.solenoidAccessoiries.get(addrs);
          StatusType status = SolenoidAccessoiry.getStatusType(accessoiries.get(addrs));
          sastm.put(sa, status);
        }

        DriveWay dw = new DriveWay(address, name, description, type, sastm, trackStatus);
        driveWays.put(address, dw);
      }
    }
  }

  @Override
  public Map<Integer, DriveWay> getDriveWays() {
    Map<Integer, DriveWay> dwm = new HashMap<>();

    this.driveWays.values().forEach((dw) -> {
      DriveWay dwc = dw.copy();
      dwc.addAttributeChangeListener(this);
      dwm.put(dw.getAddress(), dwc);
    });

    return dwm;
  }

  @Override
  public Map<Integer, DriveWay> getDriveWayTracks() {
    Map<Integer, DriveWay> dwm = new HashMap<>();

    this.driveWays.values().forEach((dw) -> {
      if (dw.isTrack()) {
        DriveWay dwt = dw.copy();
        dwt.addAttributeChangeListener(this);
        dwm.put(dw.getAddress(), dwt);
      }
    });

    return dwm;
  }

  @Override
  public DriveWay getDriveWay(Integer address) {
    DriveWay dw = this.driveWays.get(address);
    if (dw != null) {
      dw = dw.copy();
      dw.addAttributeChangeListener(this);
    }
    return dw;
  }

  public static void main(String[] a) {
    XmlRepository repo = new XmlRepository();

//    System.out.println("=============");
//    for (SolenoidAccessoiry s : repo.getSolenoidAccessoiries().values()) {
//      System.out.println(s);
//    }
    System.out.println("=============");

    for (DriveWay dw : repo.getDriveWays().values()) {
      System.out.println(dw + " Type: " + dw.getType());

    }
  }

  @Override
  public Map<Integer, Locomotive> getLocomotives() {
    //Create a copy
    Map<Integer, Locomotive> clm = new HashMap<>();

    locomotives.values().forEach((loco) -> {
      Locomotive l = loco.copy();
      l.addAttributeChangeListener(this);
      clm.put(loco.getAddress(), l);
    });

    return clm;
  }

  @Override
  public Map<Integer, Crane> getCranes() {
    Map<Integer, Crane> ccm = new HashMap<>();
    cranes.values().forEach((crane) -> {
      Crane c = crane.copy();
      c.addAttributeChangeListener(this);
      ccm.put(crane.getAddress(), c);
    });
    return ccm;
  }

  @Override
  public Map<Integer, FeedbackModule> getFeedbackModules() {
    Map<Integer, FeedbackModule> fbm = new HashMap<>();
    synchronized (feedbackModules) {
      feedbackModules.values().forEach((FeedbackModule feedbackModule) -> {
        FeedbackModule fm = feedbackModule.copy();
        fm.addAttributeChangeListener(this);
        fbm.put(feedbackModule.getAddress(), fm);
      });
    }
    return fbm;
  }

  @Override
  public FeedbackModule getFeedbackModule(Integer moduleNumber) {
    FeedbackModule fm = feedbackModules.get(moduleNumber);
    FeedbackModule fmc = null;
    if (fm != null) {
      fmc = fm.copy();
      fmc.addAttributeChangeListener(this);
    }
    return fmc;
  }

  @Override
  public Map<Integer, SolenoidAccessoiry> getSolenoidAccessoiries() {
    Map<Integer, SolenoidAccessoiry> sas = new HashMap<>();
    this.solenoidAccessoiries.keySet().forEach((address) -> {
      SolenoidAccessoiry sa = solenoidAccessoiries.get(address).copy();
      sa.addAttributeChangeListener(this);
      sas.put(address, sa);
    });
    return sas;
  }

  @Override
  public SolenoidAccessoiry getSolenoidAccessoiry(Integer address) {
    SolenoidAccessoiry sa = solenoidAccessoiries.get(address);
    if (sa != null) {
      sa = sa.copy();
      sa.addAttributeChangeListener(this);
    }
    return sa;
  }

  @Override
  public Locomotive getLocomotive(Integer address) {
    Locomotive l = this.locomotives.get(address);
    if (l != null) {
      l = l.copy();
      l.addAttributeChangeListener(this);
    }
    return l;
  }

  @Override
  public Crane getCrane(Integer address) {
    Crane c = this.cranes.get(address).copy();
    if (c != null) {
      c.addAttributeChangeListener(this);
    }
    return c;
  }

  @Override
  public void addController(ControllerProvider controller) {
    this.controllers.add(controller);
  }

  @Override
  public void removeController(ControllerProvider controller) {
    this.controllers.remove(controller);
  }

  private void updateLocomotive(AttributeChangedEvent evt) {
    Locomotive chLoc = (Locomotive) evt.getSource();
    Locomotive curLoc = this.locomotives.get(chLoc.getAddress());

    if (!chLoc.equals(curLoc)) {
      synchronized (curLoc) {
        if ("changeDirection".equals(evt.getAttribute())) {
          curLoc.setDirection((Direction) evt.getNewValue());
        }
        if ("setDirection".equals(evt.getAttribute())) {
          curLoc.setDirection((Direction) evt.getNewValue());
        }
        if ("setSpeed".equals(evt.getAttribute())) {
          curLoc.setSpeed((Integer) evt.getNewValue());
        }
        if ("stop".equals(evt.getAttribute())) {
          curLoc.setSpeed((Integer) evt.getNewValue());
        }
        if ("setThrottle".equals(evt.getAttribute())) {
          curLoc.setThrottle((Integer) evt.getNewValue());
        }
        if ("setSelected".equals(evt.getAttribute())) {
          curLoc.setSelected((Boolean) evt.getNewValue());
        }
        if ("setF0".equals(evt.getAttribute())) {
          curLoc.setF0((Boolean) evt.getNewValue());
        }
        if ("setF1".equals(evt.getAttribute())) {
          curLoc.setF1((Boolean) evt.getNewValue());
        }
        if ("setF2".equals(evt.getAttribute())) {
          curLoc.setF2((Boolean) evt.getNewValue());
        }
        if ("setF3".equals(evt.getAttribute())) {
          curLoc.setF3((Boolean) evt.getNewValue());
        }
        if ("setF4".equals(evt.getAttribute())) {
          curLoc.setF4((Boolean) evt.getNewValue());
        }
        this.locomotives.put(curLoc.getAddress(), curLoc);
      }

      Logger.trace("Updated: " + evt);
    }
  }

  private void updateSolenoidAccessoiry(AttributeChangedEvent evt) {
    SolenoidAccessoiry chSa = (SolenoidAccessoiry) evt.getSource();
    SolenoidAccessoiry curSa = this.solenoidAccessoiries.get(chSa.getAddress());

    if (!chSa.equals(curSa)) {
      if ("setStatus".equals(evt.getAttribute())) {
        String key = SolenoidAccessoiry.class.getSimpleName() + "_" + curSa.getType() + "_" + curSa.getAddress();

        synchronized (curSa) {
          curSa.setStatus(chSa.getStatus());
          this.solenoidAccessoiries.put(curSa.getAddress(), curSa);
          prefs.put(key, curSa.getStatus().toString());
        }

        Logger.trace("Updated: " + evt);

        if (!trackEventListeners.isEmpty()) {
          TrackEvent te = new TrackEvent(TrackEvent.EventType.STATUS, chSa);
          for (TrackEventListener tel : this.trackEventListeners) {
            tel.trackStatusChanged(te);
          }
        }

      }
    }
  }

  private void updateFeedbackModule(AttributeChangedEvent evt) {
    FeedbackModule chFm = (FeedbackModule) evt.getSource();
    FeedbackModule curFm = feedbackModules.get(chFm.getAddress());

    this.fbToggle = !this.fbToggle;

    if (!chFm.equals(curFm)) {
      if ("requestFeedback".equals(evt.getAttribute())) {
        Integer[] response = chFm.getResponse();
        if (response != null) {
          synchronized (curFm) {
            curFm.setResponse(response);
            feedbackModules.put(curFm.getAddress(), curFm);
          }

          StringBuilder sb = new StringBuilder();
          for (Integer r : response) {
            sb.append("[");
            sb.append(r);
            sb.append("]");
          }
          Logger.trace("Updated " + curFm + " -> " + sb);
        }
      }
    }
    //Alway send the current status
    if (!trackEventListeners.isEmpty()) {
      TrackEvent te = new TrackEvent(TrackEvent.EventType.FEEDBACK, chFm);
      for (TrackEventListener tel : this.trackEventListeners) {
        tel.trackStatusChanged(te);
      }
    }

  }

  public void addTrackEventListener(TrackEventListener listener) {
    trackEventListeners.add(listener);
    Logger.debug("Listener added. Listener count: " + trackEventListeners.size());
  }

  public void removeTrackEventListener(TrackEventListener listener) {
    trackEventListeners.remove(listener);
    Logger.debug("Listener removed. Listener count: " + trackEventListeners.size());
  }

  @Override
  public void updateControllableItem(AttributeChangedEvent evt
  ) {
    switch (evt.getItemType()) {
      case SOLENOIDACCESSOIRY:
        updateSolenoidAccessoiry(evt);
        break;
      case LOCOMOTIVE:
        updateLocomotive(evt);
        break;
      case S88:
        updateFeedbackModule(evt);
      default:
        break;
    }
    evt.getSource().setEnableAttributeChangeHandling(true);
  }

  @Override
  public ServerInfo getServerInfo() {
    return ServerInfoProvider.getServerInfo();
  }

  @Override
  public void startFeedbackCycle() {
    if (this.feedbackTimerTask == null) {
      feedbackTimerTask = new FeedbackTimerTask(this);
    }
  }

  @Override
  public void stopFeedbackCycle() {
    if (this.feedbackTimerTask != null) {
      feedbackTimerTask.stopFeedbackTimerTask();
      this.feedbackTimerTask = null;
    }
  }

  @Override
  public boolean isFeedbackCycleRunning() {
    return this.feedbackTimerTask != null;
  }

  @Override
  public boolean feedbackCycleClock() {
    return this.fbToggle;
  }

  @Override
  public void controllableItemChange(AttributeChangedEvent evt
  ) {
    //An item has changed, first check whether the change of the item should result into track command(s)
    if (!this.controllers.isEmpty()) {
      this.controllers.forEach((controller) -> {
        //Disable recursive calling
        evt.getSource().setEnableAttributeChangeHandling(false);
        //set the repo for callback
        evt.setRepository(this);
        controller.updateTrack(evt);
      });
    }
  }

}
