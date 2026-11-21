package jcs.ui;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import jcs.commandStation.lifecycle.ConnectionState;
import jcs.commandStation.automation.RailController;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lifecycle/status display tests for the NetBeans-generated StatusPanel.
 *
 * Tests the handwritten controller methods; never edits generated form fields.
 * Each assertion on Swing components runs on the Event Dispatch Thread (EDT).
 */
class StatusPanelLifecycleTest {

  private static JLabel label(StatusPanel panel, String fieldName) {
    try {
      Field field = StatusPanel.class.getDeclaredField(fieldName);
      field.setAccessible(true);
      return (JLabel) field.get(panel);
    } catch (ReflectiveOperationException ex) {
      throw new AssertionError("StatusPanel field changed: " + fieldName, ex);
    }
  }

  private static void render(StatusPanel panel, ConnectionState state, String detail) {
    try {
      Method method = StatusPanel.class.getDeclaredMethod(
              "updateConnectionState", ConnectionState.class, String.class);
      method.setAccessible(true);
      method.invoke(panel, state, detail);
    } catch (InvocationTargetException ex) {
      throw new AssertionError("Could not render " + state, ex.getCause());
    } catch (ReflectiveOperationException ex) {
      throw new AssertionError("StatusPanel lifecycle method changed", ex);
    }
  }

  private static void onEdt(Runnable assertions) throws Exception {
    if (SwingUtilities.isEventDispatchThread()) {
      assertions.run();
      return;
    }
    // invokeAndWait propagates AssertionError and RuntimeException through
    // InvocationTargetException; unwrap them for useful JUnit failures.
    try {
      SwingUtilities.invokeAndWait(assertions);
    } catch (InvocationTargetException ex) {
      if (ex.getCause() instanceof AssertionError error) {
        throw error;
      }
      if (ex.getCause() instanceof RuntimeException error) {
        throw error;
      }
      throw ex;
    }
  }

  @Test
  void rendersAllSixLifecycleStatesUsingNetBeansComponentNames() throws Exception {
    ConnectionState[] states = {
      ConnectionState.DISCONNECTED,
      ConnectionState.DISCOVERING,
      ConnectionState.CONNECTING,
      ConnectionState.CONNECTED,
      ConnectionState.RECONNECTING,
      ConnectionState.STOPPING
    };
    String[] captions = {
      "Disconnected", "Discovering...", "Connecting...",
      "Connected", "Reconnecting...", "Disconnecting..."
    };

    onEdt(() -> {
      StatusPanel panel = new StatusPanel(false);
      JLabel icon = label(panel, "statusIconLbl");
      JLabel caption = label(panel, "statusLbl");

      for (int i = 0; i < states.length; i++) {
        render(panel, states[i], "attempt " + i);
        assertEquals(captions[i], caption.getText(), "Wrong label for " + states[i]);
        assertTrue(icon.isVisible(), "Icon hidden for " + states[i]);
        assertNotNull(icon.getIcon(), "Missing icon for " + states[i]);
        assertTrue(caption.getToolTipText().contains("attempt " + i));
        assertEquals(caption.getToolTipText(), icon.getToolTipText());
      }
    });
  }

  @Test
  void clearsPreviousMeasurementsAndHidesThemDuringReconnect() throws Exception {
    onEdt(() -> {
      StatusPanel panel = new StatusPanel(false);
      JLabel current = label(panel, "currentLbl");
      JLabel voltage = label(panel, "voltageLbl");
      JLabel temperature = label(panel, "tempLbl");

      current.setText("1.8 A");
      voltage.setText("18 V");
      temperature.setText("35 C");

      render(panel, ConnectionState.RECONNECTING, "CAN ping timeout");

      assertEquals("-", current.getText());
      assertEquals("-", voltage.getText());
      assertEquals("-", temperature.getText());
      assertFalse(current.isVisible());
      assertFalse(voltage.isVisible());
      assertFalse(temperature.isVisible());
      assertTrue(label(panel, "statusLbl").getToolTipText().contains("CAN ping timeout"));
    });
  }

  @Test
  void defaultsToDisconnectedForNullStateAndUsesCaptionAsTooltip() throws Exception {
    onEdt(() -> {
      StatusPanel panel = new StatusPanel(false);
      render(panel, ConnectionState.CONNECTED, "online");
      render(panel, null, null);

      assertEquals("Disconnected", label(panel, "statusLbl").getText());
      assertEquals("Disconnected", label(panel, "statusLbl").getToolTipText());
      assertEquals("Disconnected", label(panel, "statusIconLbl").getToolTipText());
    });
  }

  @Test
  void lifecycleUpdateFromWorkerRunsOnSwingEventThread() throws Exception {
    AtomicReference<StatusPanel> holder = new AtomicReference<>();
    onEdt(() -> holder.set(new StatusPanel(false)));
    StatusPanel panel = holder.get();

    // Called from the test thread, not the EDT. Rendering must be deferred.
    assertFalse(SwingUtilities.isEventDispatchThread());
    render(panel, ConnectionState.RECONNECTING, "retry 2");

    // An invokeAndWait sentinel executes after the queued invokeLater update.
    onEdt(() -> {
      assertEquals("Reconnecting...", label(panel, "statusLbl").getText());
      assertTrue(label(panel, "statusLbl").getToolTipText().contains("retry 2"));
    });
  }

  @Test
  void automodeStatusIsIndependentOfConnectionStatus() throws Exception {
    onEdt(() -> {
      StatusPanel panel = new StatusPanel(false);
      render(panel, ConnectionState.RECONNECTING, "network lost");

      panel.onControllerStatusChange(RailController.STOPPED);

      assertTrue(label(panel, "railControllerStatusLbl").isVisible());
      assertNotNull(label(panel, "railControllerStatusLbl").getIcon());
      assertEquals("Reconnecting...", label(panel, "statusLbl").getText());
    });
  }
}
