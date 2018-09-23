/*
 * Test tom check whether the full connection to the track works
 * A 6050 must be connected a the signals , turnouts an loc shoul be on the track
 */
package lan.wervel.jcs.common;

import lan.wervel.jcs.controller.ControllerFactory;
import lan.wervel.jcs.repository.RepositoryFactory;
import lan.wervel.jcs.repository.model.DriveWay;
import lan.wervel.jcs.repository.model.FeedbackModule;
import lan.wervel.jcs.repository.model.Locomotive;
import lan.wervel.jcs.repository.model.Locomotive.Direction;
import lan.wervel.jcs.repository.model.SolenoidAccessoiry;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Logger;

/**
 *
 * @author frans
 */
public class ControllerRepoTest {

  private final TrackRepository repository;
  private final ControllerProvider controller;
  private final boolean connected;

  public ControllerRepoTest() {
    Configurator.defaultConfig().level(org.pmw.tinylog.Level.DEBUG).activate();

    repository = RepositoryFactory.getRepository();
    controller = ControllerFactory.getController();

    repository.addController(controller);
    connected = controller.connect();
  }

  public static void main(String[] a) throws InterruptedException {
    ControllerRepoTest crt = new ControllerRepoTest();
    if (crt.connected) {
      //crt.testSolenoids();
      crt.testDriveWays();
      
      //crt.testReconnect();

      //crt.testLoco();
      //crt.testFeedbackCycle();
      Thread.sleep(5000);
      crt.controller.disconnect();
    }

  }

  private void testReconnect() throws InterruptedException {
    Logger.info("*** testReconnect");

    controller.disconnect();
    Thread.sleep(3000);
    if (!controller.isConnected()) {
      Logger.info("*** Perform Reconnect.....");

      controller.connect();

      boolean ok = controller.isConnected();

      if (ok) {
        Logger.info("*** ### Reconnected");

        testSolenoids();
      }

    }

  }

  private void testSolenoids() throws InterruptedException {
    Logger.info("*** testSolenoids");

    SolenoidAccessoiry s12 = this.repository.getSolenoidAccessoiry(12);
    SolenoidAccessoiry s7 = this.repository.getSolenoidAccessoiry(7);
    SolenoidAccessoiry t10 = this.repository.getSolenoidAccessoiry(10);
    SolenoidAccessoiry t14 = this.repository.getSolenoidAccessoiry(14);
/*
    Turnout t9 = this.repository.getTurnout(9);
    Logger.info("*** Setting T9: " + (t9.isCurved() ? "Curved" : "Straight") + " to Curved...");

    long now = System.currentTimeMillis();
    t9.setCurved();
    while (!t9.isCurved()) {
      t9 = this.repository.getTurnout(9);
    }
    long finished = System.currentTimeMillis();
    Logger.info("*** T9: " + (t9.isCurved() ? "Curved" : "Straight") + " Execution time: " + (finished - now) + " ms ");

    now = System.currentTimeMillis();
    t10.setStraight();
    while (t10.isCurved()) {
      t10 = this.repository.getTurnout(10);
    }
    finished = System.currentTimeMillis();

    Logger.info("*** T10: " + (t10.isCurved() ? "Curved" : "Straight") + " Execution time: " + (finished - now) + " ms ");

    now = System.currentTimeMillis();
    s7.setGreen();
    while (s7.isRed()) {
      s7 = this.repository.getSignal(7);
    }
    finished = System.currentTimeMillis();
    Logger.info("*** S7: " + (s7.isGreen() ? "Green" : "Red") + " Execution time: " + (finished - now) + " ms ");
    now = System.currentTimeMillis();
    s12.setGreen();
    while (s12.isRed()) {
      s12 = this.repository.getSignal(12);
    }
    finished = System.currentTimeMillis();
    Logger.info("*** S12: " + (s12.isGreen() ? "Green" : "Red") + " Execution time: " + (finished - now) + " ms ");
    System.out.flush();

    Thread.sleep(1000);

    now = System.currentTimeMillis();
    s7 = this.repository.getSignal(7);
    s7.setRed();
    while (s7.isGreen()) {
      s7 = this.repository.getSignal(7);
    }
    finished = System.currentTimeMillis();
    Logger.info("*** S7: " + (s7.isGreen() ? "Green" : "Red") + " Execution time: " + (finished - now) + " ms ");
    now = System.currentTimeMillis();

    s12 = this.repository.getSignal(12);
    s12.setRed();
    while (s12.isGreen()) {
      s12 = this.repository.getSignal(12);
    }
    finished = System.currentTimeMillis();
    Logger.info("*** S12: " + (s12.isGreen() ? "Green" : "Red") + " Execution time: " + (finished - now) + " ms ");

    t9 = this.repository.getTurnout(9);
    Logger.info("*** Setting T9: " + (t9.isCurved() ? "Curved" : "Straight") + " to Straight...");
    now = System.currentTimeMillis();

    t9.setStraight();

    while (!t9.isStraight()) {
      t9 = this.repository.getTurnout(9);
    }
    finished = System.currentTimeMillis();

    Logger.info("*** T9: " + (t9.isCurved() ? "Curved" : "Straight") + " Execution time: " + (finished - now) + " ms ");

    Thread.sleep(1000);

    now = System.currentTimeMillis();
    s7 = this.repository.getSignal(7);
    s7.setGreen();
    while (s7.isRed()) {
      s7 = this.repository.getSignal(7);
    }
    finished = System.currentTimeMillis();
    Logger.info("*** S7: " + (s7.isGreen() ? "Green" : "Red") + " Execution time: " + (finished - now) + " ms ");
    now = System.currentTimeMillis();

    s12 = this.repository.getSignal(12);
    s12.setGreen();
    while (s12.isRed()) {
      s12 = this.repository.getSignal(12);
    }
    finished = System.currentTimeMillis();
    Logger.info("*** S12: " + (s12.isGreen() ? "Green" : "Red") + " Execution time: " + (finished - now) + " ms ");
    now = System.currentTimeMillis();

    t10 = this.repository.getTurnout(10);
    t10.setCurved();
    while (t10.isStraight()) {
      t10 = this.repository.getTurnout(10);
    }
    finished = System.currentTimeMillis();
    Logger.info("*** T10: " + (t10.isCurved() ? "Curved" : "Straight") + " Execution time: " + (finished - now) + " ms ");

    //Thread.sleep(1000);
*/
    t14.setCurved();
    Thread.sleep(1000);
    //t14 = this.repository.getTurnout(14);
    t14.setStraight();
    Thread.sleep(1000);
    //t14 = this.repository.getTurnout(14);
    t14.setCurved();
    Thread.sleep(1000);
    //t14 = this.repository.getTurnout(14);
    t14.setStraight();
    Thread.sleep(1000);
    //t14 = this.repository.getTurnout(14);
    t14.setCurved();

  }

  private void testLoco() throws InterruptedException {
    Thread.sleep(2000);
    Logger.info("*** testLoco");
    Locomotive loco = this.repository.getLocomotive(12);
    Logger.info("*** Setting Loco 12. Direction: " + loco.getDirection() + " Speed: " + loco.getSpeed() + " F0: " + loco.isF0() + "...");

    long start = System.currentTimeMillis();
    long now = start;
    long timeout = now + 10000;
    Logger.info("*** Setting Speed on Loco 12 to 11");
    loco.setSpeed(11);

    Locomotive l12 = this.repository.getLocomotive(12);
    while (l12.getSpeed() != 11 && now < timeout) {
      l12 = this.repository.getLocomotive(12);
      now = System.currentTimeMillis();
    }
    long finished = System.currentTimeMillis();

    Logger.info("*** Execution time: " + (finished - start) + " ms. Loco 12. Direction: " + l12.getDirection() + " Speed: " + l12.getSpeed() + " F0: " + l12.isF0() + "...");
    l12 = this.repository.getLocomotive(12);
    Thread.sleep(1000);

    loco = l12;
    start = System.currentTimeMillis();
    now = start;
    timeout = now + 10000;
    loco.setF0(true);
    Logger.info("*** Setting Function Loco 12 to True");

    l12 = this.repository.getLocomotive(12);
    while (l12.isF0() && now < timeout) {
      l12 = this.repository.getLocomotive(12);
      now = System.currentTimeMillis();
    }
    finished = System.currentTimeMillis();

    Logger.info("*** Execution time: " + (finished - start) + " ms. Loco 12. Direction: " + l12.getDirection() + " Speed: " + l12.getSpeed() + " F0: " + l12.isF0() + "...");

    Thread.sleep(2000);

    Logger.info("*** CHANGE Loco 12. Direction: " + loco.getDirection() + " Speed: " + loco.getSpeed() + " F0: " + loco.isF0() + "...");

    loco = l12;
    start = System.currentTimeMillis();
    now = start;
    timeout = now + 10000;
    loco.changeDirection();
    Logger.info("*** Setting Changing direction Loco 12 to " + loco.getDirection());

    l12 = this.repository.getLocomotive(12);
    while (!l12.getDirection().equals(Direction.Backwards) && now < timeout) {
      l12 = this.repository.getLocomotive(12);
      now = System.currentTimeMillis();
    }
    finished = System.currentTimeMillis();

    Logger.info("*** Execution time: " + (finished - start) + " ms. Loco 12. Direction: " + l12.getDirection() + " Speed: " + l12.getSpeed() + " F0: " + l12.isF0() + "...");

    Thread.sleep(2000);

    loco = l12;
    start = System.currentTimeMillis();
    now = start;
    timeout = now + 10000;
    Logger.info("*** Setting Speed on Loco 12 to 11");
    loco.setSpeed(11);

    l12 = this.repository.getLocomotive(12);
    while (l12.getSpeed() != 11 && now < timeout) {
      l12 = this.repository.getLocomotive(12);
      now = System.currentTimeMillis();
    }
    finished = System.currentTimeMillis();

    Logger.info("*** Execution time: " + (finished - start) + " ms. Loco 12. Direction: " + l12.getDirection() + " Speed: " + l12.getSpeed() + " F0: " + l12.isF0() + "...");
    Thread.sleep(1000);

    loco = l12;
    start = System.currentTimeMillis();
    now = start;
    timeout = now + 10000;
    loco.setF0(false);
    Logger.info("*** Setting Function Loco 12 to False");

    l12 = this.repository.getLocomotive(12);
    while (!l12.isF0() && now < timeout) {
      l12 = this.repository.getLocomotive(12);
      now = System.currentTimeMillis();
    }
    finished = System.currentTimeMillis();

    Logger.info("*** Execution time: " + (finished - start) + " ms. Loco 12. Direction: " + l12.getDirection() + " Speed: " + l12.getSpeed() + " F0: " + l12.isF0() + "...");

    Thread.sleep(2000);

    Logger.info("*** CHANGE Loco 12. Direction: " + loco.getDirection() + " Speed: " + loco.getSpeed() + " F0: " + loco.isF0() + "...");

    loco = l12;
    start = System.currentTimeMillis();
    now = start;
    timeout = now + 10000;
    loco.changeDirection();
    Logger.info("*** Setting Changing direction Loco 12 to " + loco.getDirection());

    l12 = this.repository.getLocomotive(12);
    while (l12.getDirection().equals(Direction.Backwards) && now < timeout) {
      l12 = this.repository.getLocomotive(12);
      now = System.currentTimeMillis();
    }
    finished = System.currentTimeMillis();

    Logger.info("*** Execution time: " + (finished - start) + " ms. Loco 12. Direction: " + l12.getDirection() + " Speed: " + l12.getSpeed() + " F0: " + l12.isF0() + "...");

    Thread.sleep(2000);

    loco = l12;
    Logger.info("*** ^^ Stopping Loco 12. Direction: " + loco.getDirection() + " Speed: " + loco.getSpeed() + " F0: " + loco.isF0() + "...");

    start = System.currentTimeMillis();
    now = start;
    timeout = now + 10000;
    loco.stop();
    Logger.info("*** Stopping Loco 12 to 0");

    l12 = this.repository.getLocomotive(12);
    while (l12.getSpeed() != 0 && now < timeout) {
      l12 = this.repository.getLocomotive(12);
      now = System.currentTimeMillis();
    }
    finished = System.currentTimeMillis();

    Logger.info("*** Execution time: " + (finished - start) + " ms. Loco 12. Direction: " + l12.getDirection() + " Speed: " + l12.getSpeed() + " F0: " + l12.isF0() + "...");
    Logger.info("*** ######## END LOCO #######");
  }

  @SuppressWarnings("SleepWhileInLoop")
  private void testFeedbackCycle() throws InterruptedException {
    Logger.info("*** testFeedbackCycle");
    Configurator.defaultConfig().level(org.pmw.tinylog.Level.DEBUG).activate();

    this.repository.startFeedbackCycle();
    Thread.sleep(2000);
    Logger.info("*** ##### START FEEDBACK #######\n");

    for (int i = 0; i < 10; i++) {
      FeedbackModule fm = this.repository.getFeedbackModules().get(1);
      Logger.info("##: " + i + " -> " + fm);
      Thread.sleep(2000);
    }
    Logger.info("*** ### END FEEDBACK #######\n");

    this.repository.stopFeedbackCycle();
  }
  
  
  @SuppressWarnings("SleepWhileInLoop")
  private void testDriveWays() throws InterruptedException {
    //Init
    DriveWay init = this.repository.getDriveWay(1);
    //route 2 track 1
    DriveWay rtt1 = this.repository.getDriveWay(2);
    //track 1
    DriveWay trk1 = this.repository.getDriveWay(3);
    //route 2 track 2
    DriveWay rtt2 = this.repository.getDriveWay(4);
    //track 2
    DriveWay trk2 = this.repository.getDriveWay(5);
    //route 2 track 3
    DriveWay rtt3 = this.repository.getDriveWay(6);
    //track 3
    DriveWay trk3 = this.repository.getDriveWay(7);
    //entry track 
    DriveWay entry = this.repository.getDriveWay(8);
    
    Logger.info("*** Init..");
    init.activate();
    Logger.info("*** Entry -> "+entry+" active "+entry.isActive());
    Thread.sleep(3000);
    
    rtt1.activate();
    Thread.sleep(2000);
    Logger.info("*** rtt1 -> "+rtt1+" active "+rtt1.isActive());
    entry.activate();
    Logger.info("*** Entry -> "+entry+" active "+entry.isActive());
    Thread.sleep(2000);
    entry.deActivate();
    Logger.info("*** Entry de -> "+entry+" active "+entry.isActive());
    Thread.sleep(2000);
    trk1.activate();
    Logger.info("*** trk1 -> "+trk1+" active "+trk1.isActive());
    Thread.sleep(2000);
    trk1.deActivate();
    Logger.info("*** trk1 -> "+trk1+" active "+trk1.isActive());
    
    Thread.sleep(2000);
    rtt2.activate();
    Thread.sleep(2000);
    entry.activate();
    Thread.sleep(2000);
    entry.deActivate();
    Thread.sleep(2000);
    trk2.activate();
    Thread.sleep(2000);
    trk2.deActivate();
    Thread.sleep(2000);
    
    rtt3.activate();
    Thread.sleep(2000);
    entry.activate();
    Thread.sleep(2000);
    entry.deActivate();
    Thread.sleep(2000);
    trk3.activate();
    Thread.sleep(2000);    
    trk3.deActivate();
   
  }

}
