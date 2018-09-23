/*
 * Test tom check whether the full connection to the track works
 * A 6050 must be connected a the signals , turnouts an loc shoul be on the track
 */
package lan.wervel.remote;

import lan.wervel.jcs.common.ControllerProvider;
import lan.wervel.jcs.common.TrackRepository;
import lan.wervel.jcs.repository.model.DriveWay;
import lan.wervel.jcs.repository.model.FeedbackModule;
import lan.wervel.jcs.repository.model.Locomotive;
import lan.wervel.jcs.repository.model.SolenoidAccessoiry;
import lan.wervel.jcs.util.RepoConFactory;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Logger;

/**
 *
 * @author frans
 */
public class RemoteControllerRepoTest {

  private ControllerProvider controller;
  private TrackRepository repository;

  private boolean connected;

  public static void main(String[] a) throws InterruptedException {
    Configurator.defaultConfig().level(org.pmw.tinylog.Level.DEBUG).activate();

    RemoteControllerRepoTest crt = new RemoteControllerRepoTest();

    crt.connectToRemote();

    if (crt.connected) {
      //crt.testSolenoids();

      //crt.testLoco();
      //crt.testFeedbackCycle();
      //crt.controller.disconnect();
      
      crt.testDriveWays();
    }
  }

  private void connectToRemote() {
    repository = RepoConFactory.getRepository();
    controller = RepoConFactory.getController();

//    JCSServiceFinder jcsServiceFinder = new JCSServiceFinder();
//    jcsServiceFinder.start();
//    String serviceHost = jcsServiceFinder.getHostAddress();
//    int servicePort = jcsServiceFinder.getServicePort();
//    Logger.info("*** Found host " + serviceHost + " on port " + servicePort + " as jcs-server...");
//    repository = new RemoteRepository(serviceHost, servicePort);
//    controller = new RemoteController(serviceHost, servicePort);
//    repository.addController(controller);
    if (!connected) {
      Logger.debug("Connect Provider...");
      connected = controller.connect();

      Logger.info("*** Provider Connected: " + connected);
    }
  }

  private void testSolenoids() throws InterruptedException {
    Logger.info("*** testSolenoids");

    SolenoidAccessoiry s12 = this.repository.getSolenoidAccessoiry(12);
    SolenoidAccessoiry s7 = this.repository.getSolenoidAccessoiry(7);
    SolenoidAccessoiry t9 = this.repository.getSolenoidAccessoiry(9);
    SolenoidAccessoiry t10 = this.repository.getSolenoidAccessoiry(10);
    SolenoidAccessoiry t14 = this.repository.getSolenoidAccessoiry(14);

    t9.setCurved();
    t10.setStraight();
    s7.setGreen();
    s12.setGreen();

    Thread.sleep(1000);

    s7.setRed();
    s12.setRed();
    t9.setStraight();

    Thread.sleep(1000);

    s7.setGreen();
    s12.setGreen();

    t10.setCurved();

    Thread.sleep(1000);
    t14.setStraight();

    Thread.sleep(2000);
    t14.setCurved();

    Thread.sleep(1000);
    t14.setStraight();
    Thread.sleep(1000);
    t14.setCurved();
    Thread.sleep(1000);
    t14.setStraight();

  }

  private void testLoco() throws InterruptedException {
    Logger.info("*** testLoco");
    Logger.info("*** ### START LOCO #######\n");

    Locomotive loco = this.repository.getLocomotive(12);
    loco.setSpeed(12);
    loco.setF0(false);

    Thread.sleep(3000);
    loco.setF0(true);

    Thread.sleep(3000);

    loco.changeDirection();

    Thread.sleep(1000);

    loco.setSpeed(12);

    Thread.sleep(5000);

    loco.changeDirection();
    loco.setSpeed(12);
    Thread.sleep(1000);

    loco.setSpeed(0);
    loco.setF0(false);

    Logger.info("*** ### END LOCO #######");
  }

  @SuppressWarnings("SleepWhileInLoop")
  private void testFeedbackCycle() throws InterruptedException {
    Logger.info("*** testFeedbackCycle");
    Configurator.defaultConfig().level(org.pmw.tinylog.Level.TRACE).activate();

    repository.startFeedbackCycle();

    Logger.info("*** #### START FEEDBACK #######\n");

    for (int i = 0; i < 40; i++) {
      FeedbackModule fm = this.repository.getFeedbackModules().get(1);
      Logger.info("##: " + i + " -> " + fm);
      Thread.sleep(500);
    }
    System.out.flush();
    Logger.info("*** #### END FEEDBACK #######\n");

    repository.stopFeedbackCycle();
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
    Logger.info("*** Entry -> " + entry + " active " + entry.isActive());
    Thread.sleep(3000);

    rtt1.activate();
    Thread.sleep(2000);
    Logger.info("*** rtt1 -> " + rtt1 + " active " + rtt1.isActive());
    entry.activate();
    Logger.info("*** Entry -> " + entry + " active " + entry.isActive());
    Thread.sleep(2000);
    entry.deActivate();
    Logger.info("*** Entry de -> " + entry + " active " + entry.isActive());
    Thread.sleep(2000);
    trk1.activate();
    Logger.info("*** trk1 -> " + trk1 + " active " + trk1.isActive());
    Thread.sleep(2000);
    trk1.deActivate();
    Logger.info("*** trk1 -> " + trk1 + " active " + trk1.isActive());

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
