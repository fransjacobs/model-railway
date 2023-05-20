package jcs.ui.table.cuf;

import jcs.ui.table.cuf.DispatcherAction;
import javax.swing.JPopupMenu;
import javax.swing.JComponent;
import javax.swing.JMenu;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.Component;

/**
 * A PopupManager object is a MouseAdapter that manages a popup for different components. The PopupManger is used by SwingXMLBuilder
 * to simplify popup handling.
 */
public class PopupManager extends MouseAdapter {

  /**
   * to make the handling of a JPopupMenu easy, we store ourself in our popup widget's properties
   */
  public static final String POPUPMANAGER_PROPERTY = "popupmanager";

  /**
   * our Swing popup menu
   */
  private JPopupMenu mPopup;
  /**
   * x position of the last mPopup.show()
   */
  private int mLastX;
  /**
   * y position of the last mPopup.show()
   */
  private int mLastY;

  /**
   * Constructor, creates a new PopupManager as well as a new JPopupMenu that the PopupManger handles.
   *
   * @param pTitle popup title
   */
  PopupManager(final String pTitle) {
    mPopup = new JPopupMenu(pTitle);
    // store the manager in the popup, so we can access it later when we
    // only have access to the popup widget
    mPopup.putClientProperty(POPUPMANAGER_PROPERTY, this);
  }

  /**
   * Return Swing popup widget.
   *
   * @return the JPopupMenu managed by this manager
   */
  public JPopupMenu getPopup() {
    return mPopup;
  }

  /**
   * returns the x position of the last mPopup.show();
   *
   * @return x position
   */
  public int getX() {
    return mLastX;
  }

  /**
   * returns the y position of the last mPopup.show();
   *
   * @return y position
   */
  public int getY() {
    return mLastY;
  }

  /**
   * Add a component to manage the popup for.
   *
   * @param pComponent the component we should manage
   */
  public void addManagedComponent(final JComponent pComponent) {
    pComponent.addMouseListener(this);
  }

  /**
   * Remove a component from our popup management.
   *
   * @param pComponent the component we should remove
   */
  public void removeManagedComponent(final JComponent pComponent) {
    pComponent.removeMouseListener(this);
  }

  public void mousePressed(final MouseEvent pEvent) {
    handleMouseEvent(pEvent);
  }

  public void mouseReleased(final MouseEvent pEvent) {
    handleMouseEvent(pEvent);
  }

  /**
   * Common stuff of the mouse handling.
   *
   * @param pEvent the mouse event
   */
  private void handleMouseEvent(final MouseEvent pEvent) {
    if (pEvent.isPopupTrigger()) {
      // TODO: we should support "context sensitivity" by enabling/removing entries
      //       from our popup depending on information about the component that
      //       triggered the MouseEvent.
      //       A possible (simple ) solution would be that one delegate can be registered
      //       at the popupmanager and get a callback (including veto rights) just before
      //       we call show()
      mLastX = pEvent.getX();
      mLastY = pEvent.getY();
      mPopup.show(pEvent.getComponent(), mLastX, mLastY);
    }
  }

  /**
   * Small helper method to get the trigger of a popup action. We assume that the parent of the trigger widget is always a
   * JPopupMenu, and that all involved popup menus are chained by getInvoker().
   *
   * @param pEvent triggered action event (may be null)
   * @return null or the swing component that triggered the popup
   */
  public static JComponent getPopupTrigger(final ActionEvent pEvent) {
    JPopupMenu popup = getRootPopup(pEvent);
    if (popup != null) {
      Component trigger = popup.getInvoker();
      if (trigger instanceof JComponent) {
        return (JComponent) trigger;
      } else {
        return null;
      }
    }
    return null;
  }

  /**
   * Small helper method to get the popup menu of a popup action. We assume that the parent of the trigger widget is always a
   * JPopupMenu, and that all involved popup menus are chained by getInvoker(). Warning: when the ActionEvent was fired by a
   * menu/toolbar entry, it returns _NOT_ null!
   *
   * @param pEvent triggered action event (may be null)
   * @return null or the JPopupMenu that triggered the popup
   */
  public static JPopupMenu getRootPopup(final ActionEvent pEvent) {
    if (pEvent == null) {
      return null;
    }

    Object source = pEvent.getSource();
    JPopupMenu mp = null;
    if (source instanceof DispatcherAction) {
      source = ((DispatcherAction) source).getRealSource();
    }
    if (source instanceof Component) {
      Component parent = ((Component) source).getParent();
      if (parent instanceof JPopupMenu) {
        mp = (JPopupMenu) parent;
        while ((mp != null)
                && (mp.getInvoker() != null)
                && (mp.getInvoker() instanceof JMenu)
                && (mp.getInvoker().getParent() != null)
                && (mp.getInvoker().getParent() instanceof JPopupMenu)) {
          mp = (JPopupMenu) mp.getInvoker().getParent();
        }
      }
    }

    return mp;
  }
}
