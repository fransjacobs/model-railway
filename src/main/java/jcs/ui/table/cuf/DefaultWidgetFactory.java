package jcs.ui.table.cuf;

import java.awt.Container;

import javax.swing.*;

import org.jdom2.Element;

/**
 * This Default-Implementation of a widget factory creates standard swing elements for each widget type. More specific widget
 * factories may extend the <code>DefaultWidgetFactory</code> and override the "create"-methods for each element.
 */
@SuppressWarnings({"JavaDoc", "UnusedDeclaration"})
public class DefaultWidgetFactory implements WidgetFactory {

  public Container create(final String pWidgetTypeName, final Element pElement, final Container pParent) {
    Container widget;

    if (pWidgetTypeName.equals(JFrame.class.getName())) {
      widget = createFrame(pElement);
    } else if (pWidgetTypeName.equals(JDialog.class.getName())) {
      widget = createDialog(pElement, pParent);
    } else if (pWidgetTypeName.equals(JPanel.class.getName())) {
      widget = createPanel(pElement);
    } else if (pWidgetTypeName.equals(JButton.class.getName())) {
      widget = createButton(pElement);
    } else if (pWidgetTypeName.equals(JLabel.class.getName())) {
      widget = createLabel(pElement);
    } else if (pWidgetTypeName.equals(JRadioButton.class.getName())) {
      widget = createRadioButton(pElement);
    } else if (pWidgetTypeName.equals(JToggleButton.class.getName())) {
      widget = createToggleButton(pElement);
    } else if (pWidgetTypeName.equals(JCheckBox.class.getName())) {
      widget = createCheckBox(pElement);
    } else if (pWidgetTypeName.equals(JTextField.class.getName())) {
      widget = createTextField(pElement);
    } else if (pWidgetTypeName.equals(JPasswordField.class.getName())) {
      widget = createPasswordField(pElement);
    } else if (pWidgetTypeName.equals(JTextArea.class.getName())) {
      widget = createTextArea(pElement);
    } else if (pWidgetTypeName.equals(JComboBox.class.getName())) {
      widget = createComboBox(pElement);
    } else if (pWidgetTypeName.equals(JSlider.class.getName())) {
      widget = createSlider(pElement);
    } else if (pWidgetTypeName.equals(JSplitPane.class.getName())) {
      widget = createSplitPane(pElement);
    } else if (pWidgetTypeName.equals(JScrollPane.class.getName())) {
      widget = createScrollPane(pElement);
    } else if (pWidgetTypeName.equals(JTabbedPane.class.getName())) {
      widget = createTabbedPane(pElement);
    } else if (pWidgetTypeName.equals(JList.class.getName())) {
      widget = createList(pElement);
    } else if (pWidgetTypeName.equals(JTable.class.getName())) {
      widget = createTable(pElement);
    } else if (pWidgetTypeName.equals(JTree.class.getName())) {
      widget = createTree(pElement);
    } else if (pWidgetTypeName.equals(JSeparator.class.getName())) {
      widget = createSeparator(pElement);
    } else if (pWidgetTypeName.equals(TitledSeparator.class.getName())) {
      widget = createSeparatorPanel(pElement);
    } else if (pWidgetTypeName.equals(JMenuBar.class.getName())) {
      widget = createMenubar(pElement);
    } else if (pWidgetTypeName.equals(JMenu.class.getName())) {
      widget = createMenu(pElement);
    } else if (pWidgetTypeName.equals(JMenuItem.class.getName())) {
      widget = createMenuItem(pElement);
    } else if (pWidgetTypeName.equals(JCheckBoxMenuItem.class.getName())) {
      widget = createCheckBoxMenuItem(pElement);
    } else if (pWidgetTypeName.equals(JToolBar.class.getName())) {
      widget = createToolbar(pElement);
    } else if (pWidgetTypeName.equals(JPopupMenu.class.getName())) {
      widget = createPopup(pElement);
    } else if (pWidgetTypeName.equals(JSpinner.class.getName())) {
      widget = createSpinner(pElement);
    } else {
      //throw SwingXMLBuilder.createException("unknown widget " + pWidgetTypeName, pElement);
      throw new RuntimeException("unknown widget " + pWidgetTypeName);
    }

    return widget;

  }

  public void notifyWidgetComplete(final String pType, final Element pElement, final Container pCurrentWidget) {
  }

  protected JFrame createFrame(final Element pElement) {
    return new JFrame();
  }

  protected JDialog createDialog(final Element pElement, final Container pParent) {
    // check if we belong to a frame
    JDialog dialog;
    if (pParent instanceof JFrame) {
      dialog = new JDialog((JFrame) pParent);
    } else {
      dialog = new JDialog();
    }

    return dialog;
  }

  protected JPanel createPanel(final Element pElement) {
    return new JPanel();
  }

  protected JButton createButton(final Element pElement) {
    return new JButton();
  }

  protected JLabel createLabel(final Element pElement) {
    return new JLabel();
  }

  protected JRadioButton createRadioButton(final Element pElement) {
    return new JRadioButton();
  }

  protected JToggleButton createToggleButton(final Element pElement) {
    return new JToggleButton();
  }

  protected JCheckBox createCheckBox(final Element pElement) {
    return new JCheckBox();
  }

  protected JTextField createTextField(final Element pElement) {
    return new JTextField();
  }

  protected JPasswordField createPasswordField(final Element pElement) {
    return new JPasswordField();
  }

  protected JTextArea createTextArea(final Element pElement) {
    JTextArea textArea = new JTextArea();
    textArea.setWrapStyleWord(true);

    return textArea;
  }

  protected JComboBox createComboBox(final Element pElement) {
    return new JComboBox();
  }

  protected JSlider createSlider(final Element pElement) {
    return new JSlider();
  }

  protected JSplitPane createSplitPane(final Element pElement) {
    // Attention: SplitPane must be constructed with a
    // Default-Orientation, otherwise the two areas of the
    // split pane are constructed with Buttons, which we
    // do not want. (see JSplitPane-Default-Constructor)
    return new JSplitPane(0);
  }

  protected JScrollPane createScrollPane(final Element pElement) {
    return new JScrollPane();
  }

  protected JTabbedPane createTabbedPane(final Element pElement) {
    return new JTabbedPane();
  }

  protected JList createList(final Element pElement) {
    return new JList();
  }

  protected JTable createTable(final Element pElement) {
    return new JTable();
  }

  protected JTree createTree(final Element pElement) {
    return new JTree();
  }

  protected JSeparator createSeparator(final Element pElement) {
    return new JSeparator();
  }

  protected TitledSeparator createSeparatorPanel(final Element pElement) {
    return new TitledSeparator();
  }

  protected JMenuBar createMenubar(final Element pElement) {
    return new JMenuBar();
  }

  protected JMenu createMenu(final Element pElement) {
    return new JMenu();
  }

  protected JMenuItem createMenuItem(final Element pElement) {
    return new JMenuItem();
  }

  protected JMenuItem createCheckBoxMenuItem(final Element pElement) {
    return new JCheckBoxMenuItem();
  }

  protected JToolBar createToolbar(final Element pElement) {
    return new JToolBar();
  }

  protected JPopupMenu createPopup(final Element pElement) {
    PopupManager popupManager = new PopupManager("en");
    return popupManager.getPopup();
  }

  protected JSpinner createSpinner(final Element pElement) {
    return new JSpinner();
  }
}
