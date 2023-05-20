package jcs.ui.table.cuf;

import java.awt.Container;
import org.jdom2.Element;

/**
 * Interface is used by the <code>VisualBuilderDelegate</code> in the process of creation of Swing-Elements. Implementations must
 * provide proper UI-Elements in accordance to the various types of Swing-Elements.
 *
 */
public interface WidgetFactory {

  /**
   * Called be the <code>VisualBuilderDelegate</code> in order to create a specific Swing-Element
   *
   * @param pType the type of the Swing-Element that should be created. The name of the Swing-class ist used as the type-argument.
   * F.i., for the creation of a <code>JTable</code>-Elememnt, the pType-argument would be "javax.swing.JTable"
   * @param pElement the XML-element in which the declaration of the Swing element is included.
   * @param pParent the parent container to the element that should be created.
   * @return a Swing or AWT widget
   */
  Container create(String pType, Element pElement, Container pParent);

  /**
   * Called when the creation process of the widget is completed. Applications may use this callback to invoke some actions on the
   * now decorated widget, e.g. to set the preferred size.
   *
   * @param pType the type of the Swing-Element that was created.
   * @param pElement the XML-element in which the declaration of the Swing element is included.
   * @param pCurrentWidget the widget that was completed.
   */
  void notifyWidgetComplete(String pType, Element pElement, Container pCurrentWidget);
}
