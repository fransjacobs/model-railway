/*
 * Test tom check whether the full connection to the track works
 * A 6050 must be connected a the signals , turnouts an loc shoul be on the track
 */
package lan.wervel.jcs.server.rmi;

import java.rmi.RemoteException;
import lan.wervel.jcs.repository.model.FeedbackModule;
import lan.wervel.jcs.repository.model.Locomotive;
import lan.wervel.jcs.repository.model.SolenoidAccessoiry;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Logger;

/**
 *
 * @author frans
 */
public class LocalControllerRepoTest {

  private static final int RMI_REGISTRY_PORT = 2024;
  private static final int RMI_SERVICE_PORT = 2025;
  
  private RMIServerManager rmiServerManager;
  
  private RMIController controller;
  private RMIRepository repository;

  private boolean connected;

  public static void main(String[] a) throws InterruptedException, RemoteException {
    Configurator.defaultConfig().level(org.pmw.tinylog.Level.TRACE).activate();

    LocalControllerRepoTest crt = new LocalControllerRepoTest();

    crt.initLocal();

    if (crt.connected) {
      crt.testSolenoids();

      //crt.testLoco();
      //crt.testFeedbackCycle();
      //crt.controller.disconnect();
    }
  }

  private void initLocal() throws RemoteException {
    rmiServerManager = new RMIServerManager(RMI_REGISTRY_PORT, RMI_SERVICE_PORT);

    repository = rmiServerManager.getLocalRepository();
    controller = rmiServerManager.getLocalController();

    if (!connected) {
      Logger.debug("Connect Provider...");
      connected = controller.connect();

      Logger.debug("Provider Connected: " + connected);
    }
  }

  private void testSolenoids() throws InterruptedException, RemoteException {
    System.out.println("testSolenoids");

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
    Thread.sleep(1000);
    t14.setCurved();

  }

  private void testLoco() throws InterruptedException, RemoteException {
    System.out.println("testLoco");
    System.out.flush();
    System.out.println("######## START LOCO #######\n");

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

    System.out.flush();
    System.out.println("######## END LOCO #######");
  }

  @SuppressWarnings("SleepWhileInLoop")
  private void testFeedbackCycle() throws InterruptedException, RemoteException {
    System.out.println("testFeedbackCycle");
    Configurator.defaultConfig().level(org.pmw.tinylog.Level.TRACE).activate();

    System.out.flush();
    System.out.println("######## START FEEDBACK #######\n");

    for (int i = 0; i < 40; i++) {
      FeedbackModule fm = this.repository.getFeedbackModules().get(1);
      Logger.info("##: " + i + " -> " + fm);
      Thread.sleep(500);
    }
    System.out.flush();
    System.out.println("######## END FEEDBACK #######\n");
  }

}
