/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lan.wervel.jcs.repository.model;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author frans
 */
public class LocomotiveTest {

  public LocomotiveTest() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of changeDirection method, of class Locomotive.
   */
  @Test
  public void testChangeDirection() {
    System.out.println("changeDirection");
    Locomotive loco = new Locomotive();
    TstAttrChangeListener tstChange = new TstAttrChangeListener();
    loco.addAttributeChangeListener(tstChange);

    assertNull(loco.getDirection());
    loco.setDirection(Locomotive.Direction.Forwards);
    assertEquals(Locomotive.Direction.Forwards, loco.getDirection());
    assertEquals("setDirection", tstChange.getAttributeChangedEvent().getAttribute());
    assertNull("Null", tstChange.getAttributeChangedEvent().getOldValue());
    assertEquals("Forwards", Locomotive.Direction.Forwards, tstChange.getAttributeChangedEvent().getNewValue());
    assertEquals("This", loco, tstChange.getAttributeChangedEvent().getSource());

    loco.changeDirection();
  
    assertEquals( Locomotive.Direction.Backwards,loco.getDirection());
    assertEquals("changeDirection", tstChange.getAttributeChangedEvent().getAttribute());
    assertEquals(Locomotive.Direction.Backwards, tstChange.getAttributeChangedEvent().getNewValue());
    assertEquals(Locomotive.Direction.Forwards, tstChange.getAttributeChangedEvent().getOldValue());
  }

  /**
   * Test of getSpeed method, of class Locomotive.
   */
  @Test
  public void testGetSpeed() {
    System.out.println("getSpeed");
    Locomotive loco = new Locomotive();
    loco.setSpeed(0);
    TstAttrChangeListener tstChange = new TstAttrChangeListener();
    loco.addAttributeChangeListener(tstChange);
    
    loco.setSpeed(100);
    assertEquals(new Integer(100), loco.getSpeed());

    assertEquals("setSpeed", tstChange.getAttributeChangedEvent().getAttribute());
    assertEquals(100, tstChange.getAttributeChangedEvent().getNewValue());
    assertEquals(0, tstChange.getAttributeChangedEvent().getOldValue());
  }
  /**
   * Test of stop method, of class Locomotive.
   */
  @Test
  public void testStop() {
    System.out.println("stop");
    Locomotive loco = new Locomotive();
    assertNull("Null", loco.getSpeed());
    
    loco.setSpeed(100);
    TstAttrChangeListener tstChange = new TstAttrChangeListener();
    loco.addAttributeChangeListener(tstChange);
    assertEquals(new Integer(100), loco.getSpeed());
    loco.stop();
    assertEquals(new Integer(0), loco.getSpeed());
    assertEquals("stop", tstChange.getAttributeChangedEvent().getAttribute());
    assertEquals(0, tstChange.getAttributeChangedEvent().getNewValue());
    assertEquals(100, tstChange.getAttributeChangedEvent().getOldValue());
  }

  /**
   * Test of F0 method, of class Locomotive.
   */
  @Test
  public void testF0() {
    System.out.println("F0");
    Locomotive loco = new Locomotive();
    TstAttrChangeListener tstChange = new TstAttrChangeListener();
    loco.addAttributeChangeListener(tstChange);
    assertFalse("default is false", loco.isF0());

    loco.setF0(true);
    assertTrue("True", loco.isF0());
    assertEquals("setF0", tstChange.getAttributeChangedEvent().getAttribute());
    assertEquals(true, tstChange.getAttributeChangedEvent().getNewValue());
    assertEquals(false, tstChange.getAttributeChangedEvent().getOldValue());
  }

  /**
   * Test of isF1 method, of class Locomotive.
   */
  @Test
  public void testF1() {
    System.out.println("F1");
    Locomotive loco = new Locomotive();
    TstAttrChangeListener tstChange = new TstAttrChangeListener();
    loco.addAttributeChangeListener(tstChange);
    assertFalse("default is false", loco.isF1());

    loco.setF1(true);
    assertTrue("True", loco.isF1());
    assertEquals("setF1", tstChange.getAttributeChangedEvent().getAttribute());
    assertEquals(true, tstChange.getAttributeChangedEvent().getNewValue());
    assertEquals(false, tstChange.getAttributeChangedEvent().getOldValue());
  }

  /**
   * Test of isF2 method, of class Locomotive.
   */
  @Test
  public void testF2() {
    System.out.println("F2");
    Locomotive loco = new Locomotive();
    TstAttrChangeListener tstChange = new TstAttrChangeListener();
    loco.addAttributeChangeListener(tstChange);
    assertFalse("default is false", loco.isF2());

    loco.setF2(true);
    assertTrue("True", loco.isF2());
    assertEquals("setF2", tstChange.getAttributeChangedEvent().getAttribute());
    assertEquals(true, tstChange.getAttributeChangedEvent().getNewValue());
    assertEquals(false, tstChange.getAttributeChangedEvent().getOldValue());
  }

  /**
   * Test of isF3 method, of class Locomotive.
   */
  @Test
  public void testF3() {
    System.out.println("F3");
    Locomotive loco = new Locomotive();
    TstAttrChangeListener tstChange = new TstAttrChangeListener();
    loco.addAttributeChangeListener(tstChange);
    assertFalse("default is false", loco.isF3());

    loco.setF3(true);
    assertTrue("True", loco.isF3());
    assertEquals("setF3", tstChange.getAttributeChangedEvent().getAttribute());
    assertEquals(true, tstChange.getAttributeChangedEvent().getNewValue());
    assertEquals(false, tstChange.getAttributeChangedEvent().getOldValue());
  }

  /**
   * Test of isF4 method, of class Locomotive.
   */
  @Test
  public void testF4() {
    System.out.println("F4");
    Locomotive loco = new Locomotive();
    TstAttrChangeListener tstChange = new TstAttrChangeListener();
    loco.addAttributeChangeListener(tstChange);
    assertFalse("default is false", loco.isF4());

    loco.setF4(true);
    assertTrue("True", loco.isF4());
    assertEquals("setF4", tstChange.getAttributeChangedEvent().getAttribute());
    assertEquals(true, tstChange.getAttributeChangedEvent().getNewValue());
    assertEquals(false, tstChange.getAttributeChangedEvent().getOldValue());
  }

  /**
   * Test of toString method, of class Locomotive.
   */
  @Test
  public void testToString() {
    System.out.println("toString");
    Locomotive instance = new Locomotive();
    String expResult = "Locomotive ?";
    String result = instance.toString();
    assertEquals(expResult, result);
  }

  /**
   * Test of hashCode method, of class Locomotive.
   */
  @Test
  public void testHashCode() {
    System.out.println("hashCode");
    Locomotive loco = new Locomotive();
    //Important is that the hashcode diffes when an attribute is changed
    int expResult = loco.hashCode();

    loco.setSelected(true);
    assertNotEquals(expResult, loco.hashCode());

    int expResult2 = loco.hashCode();

    loco.setSelected(false);
    assertEquals(expResult, loco.hashCode());

    loco.setAddress(40);
    assertNotEquals(expResult, loco.hashCode());
    assertNotEquals(expResult2, loco.hashCode());
  }

  /**
   * Test of equals method, of class Locomotive.
   */
  @Test
  public void testEquals() {
    System.out.println("equals");
    Locomotive l1 = new Locomotive();
    Locomotive l2 = new Locomotive();

    assertTrue("True", l1.equals(l2));
    l1.setF0(true);
    assertFalse("False", l1.equals(l2));
    l2.setF1(true);
    assertFalse("False", l1.equals(l2));
    l2.setF0(true);
    l1.setF1(true);
    assertTrue("True", l1.equals(l2));
    l1.setAddress(50);
    assertFalse("False", l1.equals(l2));
    l2.setAddress(50);
    assertTrue("True", l1.equals(l2));
  }

  private class TstAttrChangeListener implements AttributeChangeListener {

    AttributeChangedEvent attributeChangedEvent;

    TstAttrChangeListener() {

    }

    @Override
    public void controllableItemChange(AttributeChangedEvent evt) {
      this.attributeChangedEvent = evt;
    }

    public AttributeChangedEvent getAttributeChangedEvent() {
      return this.attributeChangedEvent;
    }

  }

}
