/*
 * Copyright 2024 frans.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcs.ui;

import com.shinyhut.vernacular.client.VernacularClient;
import com.shinyhut.vernacular.client.VernacularConfig;
import static com.shinyhut.vernacular.client.rendering.ColorDepth.BPP_24_TRUE;
import static java.awt.BorderLayout.CENTER;
import static java.awt.Color.DARK_GRAY;
import static java.awt.Color.LIGHT_GRAY;
import static java.awt.Cursor.getDefaultCursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR;
import static java.awt.Toolkit.getDefaultToolkit;
import java.awt.datatransfer.Clipboard;
import static java.awt.datatransfer.DataFlavor.stringFlavor;
import java.awt.datatransfer.StringSelection;
import static java.lang.Math.min;
import static java.lang.Thread.sleep;
import java.net.InetAddress;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jcs.commandStation.esu.ecos.net.EcosConnectionFactory;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class VNCPanel extends javax.swing.JPanel {

  private Image lastFrame;
  private VernacularConfig config;
  private VernacularClient client;

  /**
   * Creates new form VNCPanel
   */
  public VNCPanel() {
    initComponents();
    initVnc();
  }

  private void initVnc() {
    addDrawingSurface();
    initialiseVernacularClient();
    //clipboardMonitor.start();

  }

  private void addDrawingSurface() {
    this.viewerPanel.add(new JPanel() {
      @Override
      public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int width = viewerPanel.getWidth();
        int height = viewerPanel.getHeight();
        Logger.trace("W: " + width + " H: " + height);
        if (lastFrame != null) {
          g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
          g2.drawImage(lastFrame, 0, 0, width, height, null);
        } else {
          String message = "No connection. Use \"File > Connect\" to connect to a VNC server.";
          int messageWidth = g2.getFontMetrics().stringWidth(message);
          g2.setColor(DARK_GRAY);
          g2.fillRect(0, 0, width, height);
          g2.setColor(LIGHT_GRAY);
          g2.drawString(message, width / 2 - messageWidth / 2, height / 2);
        }
      }
    }, CENTER);
  }

  private void initialiseVernacularClient() {
    config = new VernacularConfig();
    config.setColorDepth(BPP_24_TRUE);
    config.setErrorListener(e -> {
      showMessageDialog(this, e.getMessage(), "Error", ERROR_MESSAGE);
      resetUI();
    });
    //config.setUsernameSupplier(this::showUsernameDialog);
    //config.setPasswordSupplier(this::showPasswordDialog);
    config.setScreenUpdateListener(this::renderFrame);
    config.setMousePointerUpdateListener((p, h) -> this.setCursor(getDefaultToolkit().createCustomCursor(p, h, "vnc")));
    config.setBellListener(v -> getDefaultToolkit().beep());
    config.setRemoteClipboardListener(t -> getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(t), null));
    //config.setUseLocalMousePointer(localCursorMenuItem.isSelected());
    config.setUseLocalMousePointer(true);

    config.setEnableCopyrectEncoding(true);
    config.setEnableRreEncoding(true);
    config.setEnableHextileEncoding(true);
    config.setEnableZLibEncoding(false);

    client = new VernacularClient(config);
  }

  private boolean resizeRequired(Image frame) {
    return lastFrame == null || lastFrame.getWidth(null) != frame.getWidth(null) || lastFrame.getHeight(null) != frame.getHeight(null);
  }

  private void renderFrame(Image frame) {
    if (resizeRequired(frame)) {
      resizeWindow(frame);
    }
    lastFrame = frame;
    repaint();
  }

  private void resizeWindow(Image frame) {
    int remoteWidth = frame.getWidth(null);
    int remoteHeight = frame.getHeight(null);
    Rectangle screenSize = getLocalGraphicsEnvironment().getMaximumWindowBounds();
    //int paddingTop = getHeight() - getContentPane().getHeight();
    //int paddingSides = getWidth() - getContentPane().getWidth();
    //int maxWidth = (int) screenSize.getWidth() - paddingSides;

    int paddingTop = getHeight() - this.viewerPanel.getHeight();
    int paddingSides = getWidth() - this.viewerPanel.getWidth();
    int maxWidth = (int) screenSize.getWidth() - paddingSides;

    int maxHeight = (int) screenSize.getHeight() - paddingTop;
    if (remoteWidth <= maxWidth && remoteHeight < maxHeight) {
      setWindowSize(remoteWidth, remoteHeight);
    } else {
      double scale = min((double) maxWidth / remoteWidth, (double) maxHeight / remoteHeight);
      int scaledWidth = (int) (remoteWidth * scale);
      int scaledHeight = (int) (remoteHeight * scale);
      setWindowSize(scaledWidth, scaledHeight);
    }
    //setLocationRelativeTo(null);
  }

  private void setWindowSize(int width, int height) {
    this.viewerPanel.setPreferredSize(new Dimension(width, height));
    //pack();
  }

  private void resetUI() {
    //setMenuState(false);
    setCursor(getDefaultCursor());
    //setSize(800, 600);
    //setLocationRelativeTo(null);
    lastFrame = null;
    repaint();
  }

  private boolean connected() {
    return client != null && client.isRunning();
  }

  private void connect(String host, int port) {
    //setMenuState(true);
    lastFrame = null;
    client.start(host, port);
  }

  private void disconnect() {
    if (connected()) {
      client.stop();
    }
    //resetUI();
  }

  private int scaleMouseX(int x) {
    if (lastFrame == null) {
      return x;
    }
    return (int) (x * ((double) lastFrame.getWidth(null) / this.viewerPanel.getWidth()));
  }

  private int scaleMouseY(int y) {
    if (lastFrame == null) {
      return y;
    }
    return (int) (y * ((double) lastFrame.getHeight(null) / this.viewerPanel.getHeight()));
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    menuPanel = new javax.swing.JPanel();
    jToolBar1 = new javax.swing.JToolBar();
    jButton1 = new javax.swing.JButton();
    jButton2 = new javax.swing.JButton();
    jComboBox1 = new javax.swing.JComboBox<>();
    viewerPanel = new javax.swing.JPanel();

    setPreferredSize(new java.awt.Dimension(1024, 700));
    setLayout(new java.awt.BorderLayout());

    menuPanel.setPreferredSize(new java.awt.Dimension(1024, 40));
    java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 1, 1);
    flowLayout1.setAlignOnBaseline(true);
    menuPanel.setLayout(flowLayout1);

    jToolBar1.setRollover(true);

    jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/media/connect-24.png"))); // NOI18N
    jButton1.setFocusable(false);
    jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    jToolBar1.add(jButton1);

    menuPanel.add(jToolBar1);

    jButton2.setText("jButton2");
    menuPanel.add(jButton2);

    jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
    menuPanel.add(jComboBox1);

    add(menuPanel, java.awt.BorderLayout.NORTH);

    viewerPanel.setPreferredSize(new java.awt.Dimension(1024, 600));
    viewerPanel.addAncestorListener(new javax.swing.event.AncestorListener() {
      public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
        viewerPanelAncestorAdded(evt);
      }
      public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
      }
      public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
      }
    });
    viewerPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
      public void mouseDragged(java.awt.event.MouseEvent evt) {
        viewerPanelMouseDragged(evt);
      }
      public void mouseMoved(java.awt.event.MouseEvent evt) {
        viewerPanelMouseMoved(evt);
      }
    });
    viewerPanel.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
      public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
        viewerPanelMouseWheelMoved(evt);
      }
    });
    viewerPanel.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mousePressed(java.awt.event.MouseEvent evt) {
        viewerPanelMousePressed(evt);
      }
      public void mouseReleased(java.awt.event.MouseEvent evt) {
        viewerPanelMouseReleased(evt);
      }
    });
    viewerPanel.setLayout(new javax.swing.BoxLayout(viewerPanel, javax.swing.BoxLayout.LINE_AXIS));
    add(viewerPanel, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  private void viewerPanelMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewerPanelMouseMoved
    if (connected()) {
      client.moveMouse(scaleMouseX(evt.getX()), scaleMouseY(evt.getY()));
    }
  }//GEN-LAST:event_viewerPanelMouseMoved

  private void viewerPanelMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewerPanelMouseDragged
    viewerPanelMouseMoved(evt);
  }//GEN-LAST:event_viewerPanelMouseDragged

  private void viewerPanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewerPanelMousePressed
    if (connected()) {
      client.updateMouseButton(evt.getButton(), true);
    }
  }//GEN-LAST:event_viewerPanelMousePressed

  private void viewerPanelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewerPanelMouseReleased
    if (connected()) {
      client.updateMouseButton(evt.getButton(), false);
    }
  }//GEN-LAST:event_viewerPanelMouseReleased

  private void viewerPanelMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_viewerPanelMouseWheelMoved
    if (connected()) {
      int notches = evt.getWheelRotation();
      if (notches < 0) {
        client.scrollUp();
      } else {
        client.scrollDown();
      }
    }
  }//GEN-LAST:event_viewerPanelMouseWheelMoved

  private void viewerPanelAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_viewerPanelAncestorAdded
    evt.getComponent().requestFocusInWindow();
  }//GEN-LAST:event_viewerPanelAncestorAdded

  private volatile boolean shutdown = false;
  private final Thread clipboardMonitor = new Thread(() -> {
    Clipboard clipboard = getDefaultToolkit().getSystemClipboard();

    String lastText = null;
    while (!shutdown) {
      try {
        if (connected()) {
          String text = (String) clipboard.getData(stringFlavor);
          if (text != null && !text.equals(lastText)) {
            client.copyText(text);
            lastText = text;
          }
        }
        sleep(100L);
      } catch (Exception ignored) {
      }
    }
  });


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton jButton1;
  private javax.swing.JButton jButton2;
  private javax.swing.JComboBox<String> jComboBox1;
  private javax.swing.JToolBar jToolBar1;
  private javax.swing.JPanel menuPanel;
  private javax.swing.JPanel viewerPanel;
  // End of variables declaration//GEN-END:variables

//Testing
  public static void main(String args[]) {
    try {
      UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      Logger.error("Can't set the LookAndFeel: " + ex);
    }

    java.awt.EventQueue.invokeLater(() -> {
      VNCPanel vncPanel = new VNCPanel();
      JFrame testFrame = new JFrame("VNCPanel Tester");

      //this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/media/jcs-train-64.png")));
      //URL iconUrl = KeyboardSensorPanel.class.getResource("/media/jcs-train-2-512.png");
      URL iconUrl = KeyboardSensorPanel.class.getResource("/media/jcs-train-64.png");
      if (iconUrl != null) {
        testFrame.setIconImage(new ImageIcon(iconUrl).getImage());
      }

      JFrame.setDefaultLookAndFeelDecorated(true);
      testFrame.add(vncPanel);

      testFrame.addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosing(java.awt.event.WindowEvent e) {
          System.exit(0);
        }
      });

      //String host = "192.168.1.110";
      String host;
      InetAddress ia = EcosConnectionFactory.discoverEcos();
      //InetAddress ia = CSConnectionFactory.discoverCs();

      if (ia != null) {
        host = ia.getHostAddress();
      } else {
        Logger.warn("Use a default host ip.....");
        host = "192.168.1.110";
      }

      int port = 5900;
      vncPanel.connect(host, port);

      testFrame.pack();
      testFrame.setLocationRelativeTo(null);
      testFrame.setVisible(true);
    });
  }

}
