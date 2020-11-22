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
package lan.wervel.jcs.ui.util;

import com.apple.eawt.Application;
import com.thizzer.jtouchbar.JTouchBar;
import com.thizzer.jtouchbar.common.Image;
import com.thizzer.jtouchbar.item.TouchBarItem;
import com.thizzer.jtouchbar.item.view.TouchBarButton;
import com.thizzer.jtouchbar.item.view.TouchBarTextField;
import com.thizzer.jtouchbar.item.view.TouchBarView;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import lan.wervel.jcs.JCSGUI;
import org.pmw.tinylog.Logger;

/**
 *
 * @author frans
 */
public class MacOsAdapter implements InvocationHandler {

    private Object eawtApplication;
    private UICallback uiCallback;
    private JTouchBar touchBar;

    public MacOsAdapter() {
        init();
    }

    public static void setMacOsProperties() {
        System.setProperty("apple.awt.application.name", "JCS");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
    }

    public void setUiCallback(UICallback uiCallback) {
        this.uiCallback = uiCallback;
    }

    private void init() {
        try {
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.warn("Can't set the LookAndFeel: " + ex);
        }
        try {
            Application application = Application.getApplication();

            Class<?> quitHandler = findHandlerClass("QuitHandler");
            Class<?> aboutHandler = findHandlerClass("AboutHandler");
            Class<?> openFilesHandler = findHandlerClass("OpenFilesHandler");
            Class<?> preferencesHandler = findHandlerClass("PreferencesHandler");

            Object proxy = Proxy.newProxyInstance(MacOsAdapter.class.getClassLoader(), new Class<?>[]{
                quitHandler, aboutHandler, openFilesHandler, preferencesHandler}, this);

            if (application != null) {
                BufferedImage img = ImageIO.read(JCSGUI.class.getResource("/media/jcs-train-64.png"));
                // set a nice picture of JCS as dock icon.
                application.setDockIconImage(img);

                if (!GraphicsEnvironment.isHeadless()) {
                    setHandlers(Desktop.class, quitHandler, aboutHandler, openFilesHandler, preferencesHandler, proxy, Desktop.getDesktop());
                }
            }

            initTouchBar();
        } catch (ReflectiveOperationException | SecurityException | IllegalArgumentException | IOException ex) {
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
            Image locomotiveImage = new Image(new DataInputStream(MacOsAdapter.class.getResourceAsStream("/media/electric-loc-yellow-24.png")));
            Image turnoutImage = new Image(new DataInputStream(MacOsAdapter.class.getResourceAsStream("/media/turnout-yellow-24.png")));
            Image signalImage = new Image(new DataInputStream(MacOsAdapter.class.getResourceAsStream("/media/signal-yellow-24.png")));
            Image diagnosticsImage = new Image(new DataInputStream(MacOsAdapter.class.getResourceAsStream("/media/stethoscope-yellow-24.png")));
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

//      TouchBarButton overviewButton = new TouchBarButton();
//      overviewButton.setImage(displayLayoutImage);
//      overviewButton.setAction((TouchBarView view) -> {
//        Logger.trace("Touchbar Overview button clicked...");
//        JCSGUI.getJCSFrame().showDisplayLayoutPanel();
//      });
//      touchBar.addItem(new TouchBarItem("overviewButton", overviewButton, true));
            TouchBarButton locoButton = new TouchBarButton();
            locoButton.setImage(locomotiveImage);
            locoButton.setAction((TouchBarView view) -> {
                Logger.trace("Touchbar Loco button clicked...");
                JCSGUI.getJCSFrame().showLocomotives();
            });
            touchBar.addItem(new TouchBarItem("locoButton", locoButton, true));

            TouchBarButton turnoutsButton = new TouchBarButton();
            turnoutsButton.setImage(turnoutImage);
            turnoutsButton.setAction((TouchBarView view) -> {
                Logger.trace("Touchbar Turnouts button clicked...");
                JCSGUI.getJCSFrame().showTurnouts();
            });
            touchBar.addItem(new TouchBarItem("turnoutsButton", turnoutsButton, true));

            TouchBarButton signalsButton = new TouchBarButton();
            signalsButton.setImage(signalImage);
            signalsButton.setAction((TouchBarView view) -> {
                Logger.trace("Touchbar Signals button clicked...");
                JCSGUI.getJCSFrame().showSignals();
            });
            touchBar.addItem(new TouchBarItem("signalsButton", signalsButton, true));

            TouchBarButton diagnosticsButton = new TouchBarButton();
            //diagnosticsButton.setTitle("Diagnostics");
            diagnosticsButton.setImage(diagnosticsImage);
            diagnosticsButton.setAction((TouchBarView view) -> {
                Logger.trace("Touchbar diagnostics button clicked...");
                JCSGUI.getJCSFrame().showDiagnostics();
            });
            touchBar.addItem(new TouchBarItem("diagnosticsButton", diagnosticsButton, true));

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

    private void setHandlers(Class<?> appClass, Class<?> quitHandler, Class<?> aboutHandler, Class<?> openFilesHandler, Class<?> preferencesHandler, Object proxy, Object appInstance)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        appClass.getDeclaredMethod("setQuitHandler", quitHandler).invoke(appInstance, proxy);
        appClass.getDeclaredMethod("setAboutHandler", aboutHandler).invoke(appInstance, proxy);
        appClass.getDeclaredMethod("setOpenFileHandler", openFilesHandler).invoke(appInstance, proxy);
        appClass.getDeclaredMethod("setPreferencesHandler", preferencesHandler).invoke(appInstance, proxy);
    }

    private Class<?> findHandlerClass(String className) throws ClassNotFoundException {
        try {
            // Java 8 handlers
            return Class.forName("com.apple.eawt." + className);
        } catch (ClassNotFoundException e) {
            //Logger.trace(e);
            // Java 9 handlers
            return Class.forName("java.awt.desktop." + className);
        }
    }

    public static void enableOSXFullscreen(Window window) {
        try {
            // http://stackoverflow.com/a/8693890/2257172
            Class<?> eawtFullScreenUtilities = Class.forName("com.apple.eawt.FullScreenUtilities");
            eawtFullScreenUtilities.getDeclaredMethod("setWindowCanFullScreen",
                    Window.class, boolean.class).invoke(eawtFullScreenUtilities, window, Boolean.TRUE);
        } catch (ReflectiveOperationException | SecurityException | IllegalArgumentException e) {
            Logger.warn("Failed to register with OSX: " + e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Logger.debug("OSX handler: {0} - {1}", method.getName(), Arrays.toString(args));

        switch (method.getName()) {
            case "openFiles":
                if (args[0] != null) {
                    try {
                        Object oFiles = args[0].getClass().getMethod("getFiles").invoke(args[0]);
                        if (oFiles instanceof List) {
                            Logger.debug("Open Files...");
                            this.uiCallback.openFiles((List<File>) oFiles);
                        }
                    } catch (ReflectiveOperationException | SecurityException | IllegalArgumentException ex) {
                        Logger.warn("Failed to access open files event: " + ex);
                    }
                }
                break;
            case "handleQuitRequestWith":
                boolean closed = JCSGUI.getJCSFrame().handleQuitRequest();
                if (args[1] != null) {
                    try {
                        args[1].getClass().getDeclaredMethod(closed ? "performQuit" : "cancelQuit").invoke(args[1]);
                    } catch (IllegalAccessException e) {
                        Logger.debug(e);
                        // with Java 9, module java.desktop does not export com.apple.eawt, use new Desktop API instead
                        Class.forName("java.awt.desktop.QuitResponse").getMethod(closed ? "performQuit" : "cancelQuit").invoke(args[1]);
                    }
                }
                break;
            case "handleAbout":
                this.uiCallback.handleAbout();
                break;
            case "handlePreferences":
                this.uiCallback.handlePreferences();
                break;
            default:
                Logger.warn("OSX unsupported method: " + method.getName());
        }
        return null;
    }
}
