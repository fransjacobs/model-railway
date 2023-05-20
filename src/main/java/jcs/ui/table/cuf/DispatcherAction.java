package jcs.ui.table.cuf;

import javax.swing.AbstractAction;
import javax.swing.JToggleButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.swing.event.EventListenerList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class offers the <code>Action</code> benefits and also acts as a dispatcher. It is most useful in the context of
 * SwingXMLBuilder, and if there are e.g. both toolbar and menu entries for one action.<br>
 * DispatcherAction also adds support for "selected" buttons (through a shared ButtonModel).
 */
public class DispatcherAction extends AbstractAction {

  /**
   * Key we store the ButtonModel into.
   */
  public static final String TOGGLE_MODEL = "DispatcherAction_TOGGLE_MODEL";

  /**
   * A list of event listeners for this component.
   */
  protected EventListenerList mListenerList;

  /**
   * Null or the "real" source of an ActionEvent, only set during the event delivery.
   */
  protected Object mRealSource;

  /**
   * Default constuctor, this will create a non menubar/toolbar action, useful for pure dispatching.
   */
  public DispatcherAction() {
    mListenerList = new EventListenerList();
  }

  /**
   * Create a dispatcher action with the handed information.
   *
   * @param pEntryText null or the text of the action
   * @param pEntryAccelerator null or the action accelerator
   * @param pEntryMnemonic null or the action mnemonic, the first character is used after converting it to upper case
   * @param pEntryTooltip null or the action tooltip
   * @param pIcon null or the action icon
   * @param pIsToggle true if this is a toggle action
   * @throws IllegalArgumentException if pEntryText is null
   */
  public DispatcherAction(final String pEntryText, final String pEntryAccelerator, final String pEntryMnemonic,
          final String pEntryTooltip, final Icon pIcon, final boolean pIsToggle) {
    this();

    Integer mnemonic = null;
    if ((pEntryMnemonic != null) && (pEntryMnemonic.length() > 0)) {
      mnemonic = (int) Character.toUpperCase(pEntryMnemonic.charAt(0));
    }
    KeyStroke keyStroke = null;
    if ((pEntryAccelerator != null) && (pEntryAccelerator.length() > 0)) {
      keyStroke = KeyStroke.getKeyStroke(pEntryAccelerator);
    }
    putValue(NAME, pEntryText);
    putValue(ACCELERATOR_KEY, keyStroke);
    putValue(MNEMONIC_KEY, mnemonic);
    putValue(SHORT_DESCRIPTION, pEntryTooltip);
    putValue(SMALL_ICON, pIcon);

    if (pIsToggle) {
      putValue(TOGGLE_MODEL, new JToggleButton.ToggleButtonModel());
    }
  }

  /**
   * Returns the name of the entry for this action.
   *
   * @return null or entry name
   */
  public String getEntryText() {
    return (String) getValue(NAME);
  }

  /**
   * Returns the entry accelerator.
   *
   * @return null or entry accelerator
   */
  public String getEntryAccelerator() {
    KeyStroke keyStroke = (KeyStroke) getValue(ACCELERATOR_KEY);
    return (keyStroke == null) ? null
            : (new Character(keyStroke.getKeyChar())).toString();
  }

  /**
   * Returns the entry mnemonic.
   *
   * @return null or entry mnemonic
   */
  public String getEntryMnemonic() {
    Integer mnemonicKey = (Integer) getValue(MNEMONIC_KEY);
    return (mnemonicKey == null) ? null : mnemonicKey.toString();
  }

  /**
   * Returns the tool tip for the entry.
   *
   * @return null or entry tool tip
   */
  public String getTooltip() {
    return (String) getValue(SHORT_DESCRIPTION);
  }

  /**
   * Returns the (relativ) entry icon name.
   *
   * @return null or entry icon
   */
  public Icon getEntryIcon() {
    return (Icon) getValue(SMALL_ICON);
  }

  /**
   * Returns whether or not this action acts as a toggle entry.
   *
   * @return true if this action is a toggle action
   */
  public boolean isToggle() {
    Object toggleModel = getValue(TOGGLE_MODEL);
    return toggleModel != null;
  }

  /**
   * Returns null if isToggle() is false, otherwise a ButtonModel for the toggle state.
   *
   * @return null or a toggle model for this action
   */
  public ButtonModel getToggleModel() {
    Object toggleModel = getValue(TOGGLE_MODEL);
    return (ButtonModel) toggleModel;
  }

  /**
   * Enables or disables the action.
   *
   * @param pNewValue true to enable the action, false to disable it
   * @see javax.swing.AbstractAction#setEnabled(boolean)
   */
  public void setEnabled(final boolean pNewValue) {
    super.setEnabled(pNewValue);

    if (isToggle()) {
      getToggleModel().setEnabled(pNewValue);
    }
  }

  /**
   * Returns the toggle state of our toggle model, always false if we are not toggable.
   *
   * @return true if the toggle model is selected, false otherwise
   */
  public boolean isSelected() {
    ButtonModel model = getToggleModel();

    if (model != null) {
      return model.isSelected();
    } else {
      return false;
    }
  }

  /**
   * Sets the toggle state of our button model, does nothing if we are not toggable.
   *
   * @param pSelected new state
   */
  public void setSelected(final boolean pSelected) {
    ButtonModel model = getToggleModel();

    if (model != null) {
      model.setSelected(pSelected);
    }
  }

  /**
   * Callback method of our visual representations, dispatch the action to our listeners.
   *
   * @param pEvent the event describing the action
   */
  public void actionPerformed(final ActionEvent pEvent) {
    // Guaranteed to return a non-null array
    Object[] listeners = mListenerList.getListenerList();

    // not nice, but otherwise SwingConnectionManager can't work
    mRealSource = pEvent.getSource();

    // only works with JDK1.4
    pEvent.setSource(this);

    // Process the listeners last to first, notifying
    // those that are interested in this event
    try {
      for (int i = listeners.length - 2; i >= 0; i -= 2) {
        if (listeners[i] == ActionListener.class) {
          ((ActionListener) listeners[i + 1]).actionPerformed(pEvent);
        }
      }
    } finally {
      pEvent.setSource(mRealSource);
      mRealSource = null;
    }
  }

  /**
   * Adds an <code>ActionListener</code> to the dispatcher.
   *
   * @param pListener the <code>ActionListener</code> to be added
   */
  public void addActionListener(final ActionListener pListener) {
    mListenerList.add(ActionListener.class, pListener);
  }

  /**
   * Removes an <code>ActionListener</code> from the button. If the listener is the currently set <code>Action</code> for the
   * button, then the <code>Action</code> is set to <code>null</code>.
   *
   * @param pListener the <code>ActionListener</code> to be removed
   */
  public void removeActionListener(final ActionListener pListener) {
    mListenerList.remove(ActionListener.class, pListener);
  }

  /**
   * Due to the way SwingConnectionManager works, we must change the source of the action event during callback. This method
   * provides the "real" source of the event.
   *
   * @return null or the real source of the ActionEvent currently delivered
   */
  public Object getRealSource() {
    return mRealSource;
  }
}
