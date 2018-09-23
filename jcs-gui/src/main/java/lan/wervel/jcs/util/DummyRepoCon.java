package lan.wervel.jcs.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import lan.wervel.jcs.common.ControllerProvider;
import lan.wervel.jcs.common.TrackRepository;
import lan.wervel.jcs.repository.FeedbackTimerTask;
import lan.wervel.jcs.repository.model.AttributeChangeListener;
import lan.wervel.jcs.repository.model.AttributeChangedEvent;
import lan.wervel.jcs.repository.model.Crane;
import lan.wervel.jcs.repository.model.DriveWay;
import lan.wervel.jcs.repository.model.FeedbackModule;
import lan.wervel.jcs.repository.model.Locomotive;
import lan.wervel.jcs.repository.model.SolenoidAccessoiry;
import lan.wervel.jcs.repository.model.SolenoidAccessoiry.Type;
import lan.wervel.jcs.server.rmi.ServerInfo;
import lan.wervel.jcs.server.rmi.ServerInfoProvider;
import lan.wervel.marklin.track.monitor.TrackMonitor;
import org.pmw.tinylog.Logger;

/**
 * Dummy Implementation of the a Repository and Controller
 *
 * @author frans
 */
public class DummyRepoCon implements ControllerProvider, TrackRepository, TrackMonitor, AttributeChangeListener {

  private boolean connected;
  private boolean powerOn = true;

  private final Map<Integer, SolenoidAccessoiry> solenoidAccessoiries;
  private final Map<Integer, DriveWay> driveWays;
  private final Map<Integer, Locomotive> locomotives;
  private final Map<Integer, Crane> cranes;
  private final Map<Integer, FeedbackModule> feedbackModules;
  private FeedbackTimerTask feedbackTimerTask;

  private final List<ControllerProvider> controllers;

  private static Preferences prefs;
  private boolean fbToggle = false;

  public DummyRepoCon() {
    solenoidAccessoiries = new HashMap<>();
    driveWays = new HashMap<>();
    locomotives = new HashMap<>();
    cranes = new HashMap<>();
    feedbackModules = new HashMap<>();
    controllers = new ArrayList<>();

    prefs = Preferences.userRoot().node(this.getClass().getName());

    addTurnout(1, "L", "5118");
    addTurnout(2, "X", "5128");
    addTurnout(3, "L", "5118");
    addTurnout(4, "L", "5118");
    addTurnout(8, "R", "5119");
    addTurnout(9, "R", "5119");
    addTurnout(10, "L", "5141");
    addTurnout(11, "L", "5141");
    addTurnout(13, "R", "5142");
    addTurnout(14, "R", "5142");

    addSignal(5, "leave", "7188");
    addSignal(6, "leave", "7188");
    addSignal(7, "leave", "7188");
    addSignal(12, "block", "7188");

    addFeedbackModule(1, "S88", 16);

    addLocomotive(1, "V200 027", null, "3021", 7, "diesel", "Backwards", "N");
    addLocomotive(2, "81 002", null, "3021", 7, "steam", "Forwards", "N");
    addLocomotive(6, "44690", null, "3047", 7, "steam", "Forwards", "Y");
    addLocomotive(8, "NS 6502", "DHG 700C NS 6502", "3021", 7, "diesel", "Forwards", "Y");
    addLocomotive(11, "NS 1205", "NS 1205", "3055", 5, "electric", "Forwards", "N");
    addLocomotive(12, "E 141 015-8", "DB E 141 015-8", "3034.10", 7, "electric", "Forwards", "N");
    addLocomotive(14, "BR V36", "BR V36", "3142", null, "diesel", "Forwards", "Y");
    addLocomotive(24, "ER 20", "Hercules Police", "36793", 7, "diesel", "Forwards", "Y");

    addCrane(30, "7051", "Marklin 7051 with 7651", "7051", null);

    //Driveways
    Map<SolenoidAccessoiry, SolenoidAccessoiry.StatusType> as1 = new LinkedHashMap<>();
    as1.put(solenoidAccessoiries.get(12), SolenoidAccessoiry.StatusType.RED);
    as1.put(solenoidAccessoiries.get(5), SolenoidAccessoiry.StatusType.RED);
    as1.put(solenoidAccessoiries.get(6), SolenoidAccessoiry.StatusType.RED);
    as1.put(solenoidAccessoiries.get(7), SolenoidAccessoiry.StatusType.RED);
    as1.put(solenoidAccessoiries.get(10), SolenoidAccessoiry.StatusType.GREEN);
    as1.put(solenoidAccessoiries.get(11), SolenoidAccessoiry.StatusType.GREEN);
    addDriveWay(1, "INIT", "Initial track settings", "init", as1, DriveWay.TrackStatus.INIT);

    Map<SolenoidAccessoiry, SolenoidAccessoiry.StatusType> as2 = new LinkedHashMap<>();
    as2.put(solenoidAccessoiries.get(11), SolenoidAccessoiry.StatusType.GREEN);
    as2.put(solenoidAccessoiries.get(5), SolenoidAccessoiry.StatusType.RED);
    addDriveWay(2, "RTT1", "Route To Track 1", "route", as2, DriveWay.TrackStatus.ROUTING);

    Map<SolenoidAccessoiry, SolenoidAccessoiry.StatusType> as3 = new LinkedHashMap<>();
    as3.put(solenoidAccessoiries.get(5), SolenoidAccessoiry.StatusType.GREEN);
    addDriveWay(3, "TRACK1", "Track 1", "track", as3, DriveWay.TrackStatus.FREE);

    Map<SolenoidAccessoiry, SolenoidAccessoiry.StatusType> as4 = new LinkedHashMap<>();
    as4.put(solenoidAccessoiries.get(11), SolenoidAccessoiry.StatusType.RED);
    as4.put(solenoidAccessoiries.get(10), SolenoidAccessoiry.StatusType.GREEN);
    as4.put(solenoidAccessoiries.get(6), SolenoidAccessoiry.StatusType.RED);
    addDriveWay(4, "RTT2", "Route To Track 2", "route", as4, DriveWay.TrackStatus.ROUTING);

    Map<SolenoidAccessoiry, SolenoidAccessoiry.StatusType> as5 = new LinkedHashMap<>();
    as5.put(solenoidAccessoiries.get(6), SolenoidAccessoiry.StatusType.GREEN);
    addDriveWay(5, "TRACK2", "Track 2", "track", as5, DriveWay.TrackStatus.FREE);

    Map<SolenoidAccessoiry, SolenoidAccessoiry.StatusType> as6 = new LinkedHashMap<>();
    as6.put(solenoidAccessoiries.get(11), SolenoidAccessoiry.StatusType.RED);
    as6.put(solenoidAccessoiries.get(10), SolenoidAccessoiry.StatusType.RED);
    as6.put(solenoidAccessoiries.get(7), SolenoidAccessoiry.StatusType.RED);
    addDriveWay(6, "RTT3", "Route To Track 3", "route", as6, DriveWay.TrackStatus.ROUTING);

    Map<SolenoidAccessoiry, SolenoidAccessoiry.StatusType> as7 = new LinkedHashMap<>();
    as7.put(solenoidAccessoiries.get(7), SolenoidAccessoiry.StatusType.GREEN);
    addDriveWay(7, "TRACK3", "Track 3", "track", as7, DriveWay.TrackStatus.FREE);

    Map<SolenoidAccessoiry, SolenoidAccessoiry.StatusType> as8 = new LinkedHashMap<>();
    as8.put(solenoidAccessoiries.get(12), SolenoidAccessoiry.StatusType.GREEN);
    addDriveWay(8, "ENTRY_TRACK", "Entry Track", "track", as8, DriveWay.TrackStatus.FREE);
  }

  private void addTurnout(Integer address, String description, String catalogNumber) {
    SolenoidAccessoiry sa = new SolenoidAccessoiry(address, description, catalogNumber, Type.TURNOUT);
    String value = prefs.get(SolenoidAccessoiry.class.getSimpleName() + "_" + sa.getType() + "_" + sa.getAddress(), "GREEN");
    sa.setStatus(value);
    solenoidAccessoiries.put(sa.getAddress(), sa);
  }

  private void addDriveWay(Integer address, String name, String description, String type, Map<SolenoidAccessoiry, SolenoidAccessoiry.StatusType> accessoirySettings, DriveWay.TrackStatus trackStatus) {
    DriveWay dw = new DriveWay(address, name, description, type, accessoirySettings, trackStatus);
    driveWays.put(dw.getAddress(), dw);
  }

  private void addSignal(Integer address, String description, String catalogNumber) {
    SolenoidAccessoiry sa = new SolenoidAccessoiry(address, description, catalogNumber, Type.SIGNAL);
    String value = prefs.get(SolenoidAccessoiry.class.getSimpleName() + "_" + sa.getType() + "_" + sa.getAddress(), "GREEN");
    sa.setStatus(value);
    solenoidAccessoiries.put(sa.getAddress(), sa);
  }

  private void addFeedbackModule(Integer moduleNumber, String catalogNumber, Integer ports) {
    FeedbackModule fm = new FeedbackModule(moduleNumber, catalogNumber, ports);
    //fm.addAttributeChangeListener(this);
    this.feedbackModules.put(fm.getAddress(), fm);
  }

  private void addLocomotive(Integer address, String name, String description, String catalogNumber, Integer minSpeed,
          String type, String defaultDirection, String specialFunctions) {
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
    loc.setSpeedSteps(14);
    loc.setSpecialFuctions("Y".equalsIgnoreCase(specialFunctions));
    loc.setF0Type("light");
    locomotives.put(loc.getAddress(), loc);
  }

  private void addCrane(Integer address, String name, String description, String catalogNumber, Integer minSpeed) {
    Crane crane = new Crane(address, name, description, catalogNumber, minSpeed, false, false, false, false, false);
    cranes.put(crane.getAddress(), crane);
  }

  @Override
  public void powerOff() {
    this.powerOn = false;
    Logger.info("Power is Off");
  }

  @Override
  public void powerOn() {
    this.powerOn = true;
    Logger.info("Power is On");
  }

  @Override
  public boolean isPowerOn() {
    return this.powerOn;
  }

  @Override
  public boolean connect() {
    this.connected = true;
    return this.connected;
  }

  @Override
  public boolean isConnected() {
    return this.connected;
  }

  @Override
  public void disconnect() {
    this.connected = false;
    Logger.info("Disconnected");
  }

  @Override
  public void updateTrack(AttributeChangedEvent evt) {
    try {
      Logger.info("Updating: " + evt);

      if (AttributeChangedEvent.Item.S88.equals(evt.getItemType())) {
        int lsb = (int) (Math.random() * 255 + 1);
        int msb = (int) (Math.random() * 255 + 1);
        Integer[] results = new Integer[2];
        results[0] = lsb;
        results[1] = msb;
        FeedbackModule s88 = (FeedbackModule) evt.getSource();

        StringBuilder sb = new StringBuilder();
        for (Integer r : results) {
          sb.append("[");
          sb.append(r);
          sb.append("]");
        }
        Logger.trace("Dummy Response for Module: " + s88.getModuleNumber() + " -> " + sb);
        s88.setResponse(results);
      }

      Thread.sleep(100);

      this.updateControllableItem(evt);
    } catch (InterruptedException ex) {
    }
  }

  @Override
  public String getName() {
    return "Dummy 6050/6051 Interface";
  }

  @Override
  public Map<Integer, Locomotive> getLocomotives() {
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

  @Override
  public Map<Integer, FeedbackModule> getFeedbackModules() {
    Map<Integer, FeedbackModule> fbm = new HashMap<>();
    feedbackModules.values().forEach((feedbackModule) -> {
      FeedbackModule fm = feedbackModule.copy();
      fm.addAttributeChangeListener(this);
      fbm.put(feedbackModule.getAddress(), fm);
    });

    return fbm;
  }

  @Override
  public FeedbackModule getFeedbackModule(Integer moduleNumber) {
    FeedbackModule fm = feedbackModules.get(moduleNumber);
    if (fm != null) {
      fm = fm.copy();
      fm.addAttributeChangeListener(this);
    }
    return fm;
  }

  private void updateLocomotive(AttributeChangedEvent evt) {
    Locomotive chLoc = (Locomotive) evt.getSource();
    Locomotive curLoc = this.locomotives.get(chLoc.getAddress());

    if (!chLoc.equals(curLoc)) {
      synchronized (curLoc) {
        if ("changeDirection".equals(evt.getAttribute())) {
          curLoc.setDirection((Locomotive.Direction) evt.getNewValue());
        }
        if ("setDirection".equals(evt.getAttribute())) {
          curLoc.setDirection((Locomotive.Direction) evt.getNewValue());
        }
        if ("setSpeed".equals(evt.getAttribute())) {
          curLoc.setSpeed((Integer) evt.getNewValue());
          System.out.println("[" + curLoc.getAddress() + "] Speed: " + evt.getNewValue());
        }
        if ("stop".equals(evt.getAttribute())) {
          curLoc.setSpeed((Integer) evt.getNewValue());
        }
        if ("setThrottle".equals(evt.getAttribute())) {
          curLoc.setThrottle((Integer) evt.getNewValue());

          //Adjust speed is neeeded
//        int steps = curLoc.getSpeedSteps();
//        int throttle = curLoc.getThrottle();
//        int max = slider.getMaximum();
//        int newSpeed = throttle / (max / steps);
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
          prefs.put(key, curSa.getStatus().toString());
          this.solenoidAccessoiries.put(curSa.getAddress(), curSa);
        }
        Logger.trace("Updated: " + evt);
      }
    }
  }

  private void updateFeedbackModule(AttributeChangedEvent evt) {
    FeedbackModule chFm = (FeedbackModule) evt.getSource();
    FeedbackModule curFm = this.feedbackModules.get(chFm.getAddress());

    this.fbToggle = !this.fbToggle;

    if (!chFm.equals(curFm)) {
      if ("requestFeedback".equals(evt.getAttribute())) {
        Integer[] response = chFm.getResponse();
        if (response != null) {

          synchronized (curFm) {
            curFm.setResponse(response);
            this.feedbackModules.put(curFm.getAddress(), curFm);
          }

          StringBuilder sb = new StringBuilder();
          for (Integer r : response) {
            sb.append("[");
            sb.append(r);
            sb.append("]");
          }
        }
      }
    }
  }

  @Override
  public void updateControllableItem(AttributeChangedEvent evt) {
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

  @Override
  public ServerInfo getServerInfo() {
    return ServerInfoProvider.getServerInfo();
  }

  @Override
  public void startFeedbackCycle() {
    Logger.info("Starting Feedback");
    if (this.feedbackTimerTask == null) {
      feedbackTimerTask = new FeedbackTimerTask(this);
    }
  }

  @Override
  public void stopFeedbackCycle() {
    Logger.info("Starting Feedback");
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
  public void controllableItemChange(AttributeChangedEvent evt) {
    //Disable recursive calling
    evt.getSource().setEnableAttributeChangeHandling(false);
    //set the repo for callback
    evt.setRepository(this);
    this.updateTrack(evt);
  }

  @Override
  public void startProcess() {
    Logger.info("Starting auto pilot...");
  }

  @Override
  public void stopProcess() {
    Logger.info("Stopping auto pilot...");
  }

  @Override
  public boolean isRunning() {
    return false;
  }

  @Override
  public List<DriveWay> getTracks() {
    List<DriveWay> tracks = new ArrayList<>();
    for (DriveWay track : this.getDriveWayTracks().values()) {
      tracks.add(track);
    }
    return tracks;
  }

  @Override
  public Integer getNextTrack() {
    return 0;
  }

}
