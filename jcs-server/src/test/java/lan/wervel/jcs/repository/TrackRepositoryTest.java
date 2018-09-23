/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lan.wervel.jcs.repository;

import lan.wervel.jcs.common.TrackRepository;
import java.util.Map;
import lan.wervel.jcs.repository.model.Crane;
import lan.wervel.jcs.repository.model.FeedbackModule;
import lan.wervel.jcs.repository.model.Locomotive;
import lan.wervel.jcs.repository.model.SolenoidAccessoiry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.pmw.tinylog.Configurator;

/**
 *
 * @author frans
 */
public class TrackRepositoryTest {

  public TrackRepositoryTest() {
  }

  @Before
  public void setUp() {
    Configurator.defaultConfig().level(org.pmw.tinylog.Level.DEBUG).activate();
  }

  @After
  public void tearDown() {
    //mySignal.setGreen();
    
    RepositoryFactory repoFactory = RepositoryFactory.getInstance();
    TrackRepository repo = repoFactory.getTrackRepository();
    repo.getSolenoidAccessoiry(12).setGreen();
    
  }

  /**
   * Test of getLocomotives method, of class XmlRepository.
   */
  @Test
  public void testGetLocomotives() {
    System.out.println("getLocomotives");
    RepositoryFactory repositoryFactory = RepositoryFactory.getInstance();
    assertNotNull("Factory should not be null!", repositoryFactory);

    TrackRepository repository = repositoryFactory.getTrackRepository();
    assertNotNull("The Repository should not be null!", repository);

    Map<Integer, Locomotive> locos = repository.getLocomotives();
    assertNotNull("Locomotives should not be null!", locos);
    assertEquals(8, locos.size());
  }

  /**
   * Test of getCranes method, of class XmlRepository.
   */
  @Test
  public void testGetCranes() {
    System.out.println("getCranes");
    RepositoryFactory repositoryFactory = RepositoryFactory.getInstance();
    assertNotNull("Factory should not be null!", repositoryFactory);

    TrackRepository repository = repositoryFactory.getTrackRepository();
    assertNotNull("The Repository should not be null!", repository);
    Map<Integer, Crane> cranes = repository.getCranes();
    assertNotNull("Cranes should not be null!", cranes);
    assertEquals(1, cranes.size());
  }

  /**
   * Test of getFeedbackModules method, of class XmlRepository.
   */
  @Test
  public void testGetFeedbackModules() {
    System.out.println("getFeedbackModules");
    RepositoryFactory repositoryFactory = RepositoryFactory.getInstance();
    assertNotNull("Factory should not be null!", repositoryFactory);

    TrackRepository repository = repositoryFactory.getTrackRepository();
    assertNotNull("The Repository should not be null!", repository);
    Map<Integer, FeedbackModule> feedbackModules = repository.getFeedbackModules();
    assertNotNull("FeedbackModules should not be null!", feedbackModules);
    assertEquals(1, feedbackModules.size());
  }

  /**
   * Test of getSignals method, of class XmlRepository.
   */
  @Test
  public void testGetSignals() {
    System.out.println("getSignals");
    RepositoryFactory repositoryFactory = RepositoryFactory.getInstance();
    assertNotNull("Factory should not be null!", repositoryFactory);

    TrackRepository repository = repositoryFactory.getTrackRepository();
    assertNotNull("The Repository should not be null!", repository);

    Map<Integer, SolenoidAccessoiry> signals = repository.getSolenoidAccessoiries();
    assertNotNull("The Signals should not be null!", signals);
    ///assertEquals(4, signals.size());
  }

  /**
   * Test of getTurnouts method, of class XmlRepository.
   */
  @Test
  public void testGetTurnouts() {
    System.out.println("getTurnouts");
    RepositoryFactory repositoryFactory = RepositoryFactory.getInstance();
    assertNotNull("Factory should not be null!", repositoryFactory);

    TrackRepository repository = repositoryFactory.getTrackRepository();
    assertNotNull("The Repository should not be null!", repository);

    Map<Integer, SolenoidAccessoiry> turnouts = repository.getSolenoidAccessoiries();
    assertNotNull("The Turnouts should not be null!", turnouts);
    //assertEquals(10, turnouts.size());
  }

  /**
   * Test of getLocomotive method, of class XmlRepository.
   */
//  @Test
//  public void testGetLocomotive() {
//    System.out.println("getLocomotive");
//    RepositoryFactory repositoryFactory = RepositoryFactory.getInstance();
//    assertNotNull("Factory should not be null!", repositoryFactory);
//    TrackRepository repository = repositoryFactory.getTrackRepository();
//    assertNotNull("The Repository should not be null!", repository);
//
//    //lan.wervel.jcs.repository.model.Locomotive<E 141 015-8 [12]>    
//    
//    Locomotive expResult = new Locomotive(12, "E 141 015-8", "DB E 141 015-8", "3034.10", 7, "electric");
//    Locomotive result = repository.getLocomotive(12);
//    //assertEquals(expResult, result);
//
//    result = repository.getLocomotive(70);
//    Assert.assertNull("There should not be a Locomotive with address 70", result);
//  }

  /**
   * Test of getCrane method, of class XmlRepository.
   */
//  @Test
//  public void testGetCrane() {
//    System.out.println("getCrane");
//    RepositoryFactory repositoryFactory = RepositoryFactory.getInstance();
//    assertNotNull("Factory should not be null!", repositoryFactory);
//
//    TrackRepository repository = repositoryFactory.getTrackRepository();
//    assertNotNull("The Repository should not be null!", repository);
//
//    Crane expResult = new Crane(30, "7051", "Marklin 7051 with 7651", "7051", null);
//
//    Crane result = repository.getCrane(30);
//    assertEquals(expResult, result);
//
//    result = repository.getCrane(24);
//    Assert.assertNull("There should not be a Locomotive with address 24", result);
//  }

  /**
   * Test of getSignal method, of class XmlRepository.
   */
  @Test
  public void testGetSignal() {
    System.out.println("getSignal");
    RepositoryFactory repositoryFactory = RepositoryFactory.getInstance();
    assertNotNull("Factory should not be null!", repositoryFactory);
    TrackRepository repository = repositoryFactory.getTrackRepository();
    assertNotNull("The Repository should not be null!", repository);

    SolenoidAccessoiry expResult = new SolenoidAccessoiry(12, "7188", "block", null);
    expResult.setGreen();
    
    SolenoidAccessoiry result = repository.getSolenoidAccessoiry(12);
    //assertEquals(expResult, result);

    result = repository.getSolenoidAccessoiry(10);
//    Assert.assertNull("There should not be a Signal with address 10", result);
  }

  /**
   * Test of getTurnout method, of class XmlRepository.
   */
  @Test
  public void testGetTurnout() {
    System.out.println("getTurnout");
    RepositoryFactory repositoryFactory = RepositoryFactory.getInstance();
    assertNotNull("Factory should not be null!", repositoryFactory);

    TrackRepository repository = repositoryFactory.getTrackRepository();
    assertNotNull("The Repository should not be null!", repository);
//    SolenoidAccessoiry expResult = new SolenoidAccessoiry(10, "5141", "L", null);
 //   expResult.setStraight();
    
    SolenoidAccessoiry result = repository.getSolenoidAccessoiry(10);
    //assertEquals(expResult, result);

    result = repository.getSolenoidAccessoiry(12);
    //Assert.assertNull("There should not be a Turnout with address 12", result);
  }

  /**
   * Test of updateControllableItem method, of class XmlRepository.
   */
//  @Test
//  public void testUpdateControllableItem() {
//    System.out.println("updateControllableItem");
//    RepositoryFactory repositoryFactory = RepositoryFactory.getInstance();
//    assertNotNull("Factory should not be null!", repositoryFactory);
//
//    TrackRepository repository = repositoryFactory.getTrackRepository();
//    assertNotNull("The Repository should not be null!", repository);
//
//    Signal mySignal = new Signal(12, "7188", "block");
//    mySignal.setGreen();
//    
//    //repository.updateControllableItem(mySignal);
//    
//    Signal s = repository.getSignal(mySignal.getAddress());
//    
//    assertEquals(mySignal, s);
//    s.setRed();
//    //repository.updateControllableItem(s);
//  
//    Signal t = repository.getSignal(mySignal.getAddress());
//  
//    assertFalse(t.isGreen());
//    assertTrue(t.isRed());
//    
//    mySignal.setRed();
//    assertEquals(mySignal, t);
//  }

}
