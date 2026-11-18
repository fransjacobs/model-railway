package jcs.commandStation.loconet;

import java.util.concurrent.atomic.AtomicInteger;
import jcs.commandStation.events.ConnectionEvent;
import jcs.entities.CommandStationBean;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** USB loss must be forwarded, not handled by an Intellibox reconnect loop. */
class IntelliboxLifecycleCallbackTest {

  @Test
  void forwardsLossToRegisteredJcsListenerWithoutOpeningPort() {
    CommandStationBean bean = new CommandStationBean();
    bean.setId("intellibox2");
    Intellibox2Impl driver = new Intellibox2Impl(bean);
    AtomicInteger callbacks = new AtomicInteger();
    driver.addConnectionEventListener(event -> {
      assertFalse(event.isConnected());
      callbacks.incrementAndGet();
    });

    driver.onConnectionChange(new ConnectionEvent("USB Intellibox", false, false));

    assertEquals(1, callbacks.get());
    assertFalse(driver.isConnected());
  }
}
