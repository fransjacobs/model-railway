/*
 * Copyright (C) 2019 frans.
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
package jcs.ui.util;

import com.thizzer.jtouchbar.JTouchBar;
import com.thizzer.jtouchbar.common.Image;
import com.thizzer.jtouchbar.item.TouchBarItem;
import com.thizzer.jtouchbar.item.view.TouchBarButton;
import com.thizzer.jtouchbar.item.view.TouchBarTextField;
import com.thizzer.jtouchbar.item.view.TouchBarView;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Taskbar;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.OpenFilesHandler;
import java.awt.desktop.PreferencesEvent;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jcs.JCSGUI;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class MacOsAdapter {

    private UICallback uiCallback;
    private JTouchBar touchBar;

    public MacOsAdapter() {
        init();
    }

    public static void setMacOsProperties() {
        System.setProperty("apple.awt.application.name", "JCS");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        //System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public void setUiCallback(UICallback uiCallback) {
        this.uiCallback = uiCallback;
    }

    private void init() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            //UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.warn("Can't set the LookAndFeel: " + ex);
        }
        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.setAboutHandler(new JCSAboutHandler());
            desktop.setQuitHandler(new JCSQuitHandler());
            desktop.setPreferencesHandler(new JCSPreferencesHandler());

            Taskbar taskbar = Taskbar.getTaskbar();
            try {
                BufferedImage img = ImageIO.read(JCSGUI.class.getResource("/media/jcs-train-64.png"));
                taskbar.setIconImage(img);
            } catch (final UnsupportedOperationException e) {
                Logger.warn("The os does not support: 'taskbar.setIconImage'");
            } catch (final SecurityException e) {
                Logger.warn("There was a security exception for: 'taskbar.setIconImage'");
            }

            initTouchBar();
        } catch (SecurityException | IllegalArgumentException | IOException ex) {
            Logger.warn("Failed to register with MacOS: " + ex);
        }
    }

    private void initTouchBar() {
        try {
            touchBar = new JTouchBar();
            touchBar.setCustomizationIdentifier("JCSTouchBar");

            //Load the images
            Image powerImage = new Image(new DataInputStream(MacOsAdapter.class.getResourceAsStream("/media/power-red-24.png")));
            //Image displayLayoutImage = new Image(new DataInputStream(MacOsAdapter.class.getResourceAsStream("/media/earth-yellow-24.png")));
            //Image locomotiveImage = new Image(new DataInputStream(MacOsAdapter.class.getResourceAsStream("/media/electric-loc-yellow-24.png")));
            //Image turnoutImage = new Image(new DataInputStream(MacOsAdapter.class.getResourceAsStream("/media/turnout-yellow-24.png")));
            //Image signalImage = new Image(new DataInputStream(MacOsAdapter.class.getResourceAsStream("/media/signal-yellow-24.png")));
            //Image diagnosticsImage = new Image(new DataInputStream(MacOsAdapter.class.getResourceAsStream("/media/stethoscope-yellow-24.png")));
            //Image designImage = new Image(new DataInputStream(MacOsAdapter.class.getResourceAsStream("/media/layout-yellow-24.png")));

            //Show label
            TouchBarTextField touchBarTextField = new TouchBarTextField();
            touchBarTextField.setStringValue("JCS");
            touchBar.addItem(new TouchBarItem("touchBarTextField", touchBarTextField, true));

            //jTouchBar.addItem(new TouchBarItem(TouchBarItem.NSTouchBarItemIdentifierFlexibleSpace));
            //jTouchBar.addItem(new TouchBarItem(TouchBarItem.NSTouchBarItemIdentifierFixedSpaceSmall));
            //Buttons
            TouchBarButton stopButton = new TouchBarButton();
            stopButton.setImage(powerImage);
            stopButton.setAction((TouchBarView view) -> {
                Logger.trace("Touchbar Stop button clicked...");
                JCSGUI.getJCSFrame().stop();
            });
            touchBar.addItem(new TouchBarItem("stopButton", stopButton, true));

            //TouchBarButton overviewButton = new TouchBarButton();
            //overviewButton.setImage(displayLayoutImage);
            //overviewButton.setAction((TouchBarView view) -> {
            //Logger.trace("Touchbar Overview button clicked...");
            //JCSGUI.getJCSFrame().showDisplayLayoutPanel();
            //});
            //touchBar.addItem(new TouchBarItem("overviewButton", overviewButton, true));
            //TouchBarButton locoButton = new TouchBarButton();
            //locoButton.setImage(locomotiveImage);
            //locoButton.setAction((TouchBarView view) -> {
            //    Logger.trace("Touchbar Loco button clicked...");
            //    JCSGUI.getJCSFrame().showLocomotives();
            //});
            //touchBar.addItem(new TouchBarItem("locoButton", locoButton, true));

            //TouchBarButton turnoutsButton = new TouchBarButton();
            //turnoutsButton.setImage(turnoutImage);
            //turnoutsButton.setAction((TouchBarView view) -> {
            //    Logger.trace("Touchbar Turnouts button clicked...");
            //    JCSGUI.getJCSFrame().showTurnouts();
            //});
            //touchBar.addItem(new TouchBarItem("turnoutsButton", turnoutsButton, true));

            //TouchBarButton signalsButton = new TouchBarButton();
            //signalsButton.setImage(signalImage);
            //signalsButton.setAction((TouchBarView view) -> {
            //    Logger.trace("Touchbar Signals button clicked...");
            //    JCSGUI.getJCSFrame().showSignals();
            //});
            //touchBar.addItem(new TouchBarItem("signalsButton", signalsButton, true));

            //TouchBarButton diagnosticsButton = new TouchBarButton();
            //diagnosticsButton.setTitle("Diagnostics");
            //diagnosticsButton.setImage(diagnosticsImage);
            //diagnosticsButton.setAction((TouchBarView view) -> {
            //    Logger.trace("Touchbar diagnostics button clicked...");
            //    JCSGUI.getJCSFrame().showDiagnostics();
            //});
            //touchBar.addItem(new TouchBarItem("diagnosticsButton", diagnosticsButton, true));

//      TouchBarButton designButton = new TouchBarButton();
//      //diagnosticsButton.setTitle("Design");
//      designButton.setImage(designImage);
//      designButton.setAction((TouchBarView view) -> {
//        Logger.trace("Touchbar design button clicked...");
//        JCSGUI.getJCSFrame().showDesignLayoutPanel();
//      });
//      touchBar.addItem(new TouchBarItem("designButton", designButton, true));
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    public void showTouchbar(Component c) {
        this.touchBar.show(c);
    }

    private class JCSQuitHandler implements QuitHandler {

        @Override
        public void handleQuitRequestWith(QuitEvent e, QuitResponse response) {
            uiCallback.handleQuitRequest();
        }
    }

    private class JCSAboutHandler implements AboutHandler {

        @Override
        public void handleAbout(AboutEvent e) {
            uiCallback.handleAbout();
        }
    }

    private class JCSPreferencesHandler implements PreferencesHandler {

        @Override
        public void handlePreferences(PreferencesEvent e) {
            uiCallback.handlePreferences();
        }
    }

    private class JCSOpenFilesHandler implements OpenFilesHandler {

        @Override
        public void openFiles(OpenFilesEvent e) {
            //STUB
            uiCallback.openFiles(null);
        }
    }

}
