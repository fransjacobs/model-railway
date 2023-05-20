package jcs.ui.table.cuf;

import jcs.util.IconCache;
import java.util.Map;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Enumeration;
import java.text.MessageFormat;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.JMenuItem;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JComponent;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * This classes helps a lot for I18N management, and acts as a decorator for Swing components.<br>
 * Instead of hard-coding a lot of resources (Tooltip, shortcut, name, ..) in Java code, use one or more resource files and
 * SwingDecorator!<br>
 * The keys in a resource file are called I18nID's, and various aspects of a component are addressed with various suffixes (.title,
 * .text, .mnemonic, .accelerator, .tooltip, .iconname).<br>
 * Example:<br>
 * A resource file contains the following lines:<br>
 * <code>
 *   myButton.text=Hello<br>
 * myButton.tooltip=just an example<br>
 * myButton.icon=myImage<br>
 * </code> The code to initialize the button would be something like:<br>
 * <code>
 *   JButton b= new JButton();<br>
 * b.setName("button");<br>
 * SwingDecorator.initilize(b, "myButton");<br>
 * </code>
 *
 * @author Jürgen Zeller, jzeller@jzeller.eu
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public class SwingDecoratorFunctionality {

  /**
   * helper to support multiple property files
   */
  private MultiResourceBundle mMultiBundle;
  /**
   * icon cache, key=icon name; value= null or ImageIcon Object
   */
  private IconCache mIconCache;
  /**
   * flag if getString is to always return a real string (and not <code>null</code>).
   */
  private boolean mEnforceGetString;

  /**
   * Standard constructor.
   */
  public SwingDecoratorFunctionality() {
    this(true, null);
  }

  /**
   * A public constructor for use in {@link net.sf.cuf.ui.builder.SwingXMLBuilder}.
   *
   * @param pEnforceSet flag if getString() is to always return a real string (and not <code>null</code>)
   * @param pIconCache icon cache (may be <code>null</code>)
   */
  public SwingDecoratorFunctionality(final boolean pEnforceSet, IconCache pIconCache) {
    if (pIconCache == null) {
      pIconCache = new IconCache();
    }

    mMultiBundle = new MultiResourceBundle();
    mIconCache = pIconCache;
    mEnforceGetString = pEnforceSet;
  }

  /**
   * Adds a property file to SwingDecorator, see ResourceBundle.getBundle() for the details how the property file is found.
   *
   * @param pBaseName base name of the resource bundle
   */
  public void addBundle(final String pBaseName) {
    mMultiBundle.addBundle(pBaseName);
  }

  /**
   * Adds all non-null String key/value pairs of a bundle to the SwingDecorator singleton.
   *
   * @param pResourceBundle the resource bundle to add, must not be null
   */
  public void addBundle(final ResourceBundle pResourceBundle) {
    mMultiBundle.addBundle(pResourceBundle);
  }

  /**
   * Returns the I18n text matching the handed key. If there is no value for the key, the key is returned.
   *
   * @param pI18nID I18n key
   * @return matching localized string or "", never null
   */
  public String getText(final String pI18nID) {
    return getText(pI18nID, null);
  }

  /**
   * Returns the I18n text matching the handed key and parameters. If there is no value for the key, the key is returned. See
   * MessageFormat.format for the formatting details.
   *
   * @param pI18nID I18n key
   * @param pParams parameters for the message
   * @return matching localized string or "", never null
   */
  public String getText(final String pI18nID, final Object[] pParams) {
    String key = pI18nID + SwingDecorator.TEXT_SUFFIX;
    return getString(key, pI18nID, pParams);
  }

  /**
   * Returns the I18n tool tip matching the handed key and parameters. If there is no value for the key, the key is returned. See
   * MessageFormat.format for the formatting details.
   *
   * @param pI18nID I18n key
   * @param pParams parameters for the message
   * @return matching tool tip or pI18nID, never null
   */
  public String getToolTip(final String pI18nID, final Object[] pParams) {
    String key = pI18nID + SwingDecorator.TOOLTIP_SUFFIX;
    return getString(key, pI18nID, pParams);
  }

  /**
   * Returns the I18n title matching the handed key. If there is no value for the key, the key is returned.
   *
   * @param pI18nID I18n key
   * @return matching title or pI18nID, never null
   */
  public String getTitle(final String pI18nID) {
    return getTitle(pI18nID, null);
  }

  /**
   * Returns the I18n title matching the handed key and parameters. If there is no value for the key, the key is returned. See
   * MessageFormat.format for the formatting details.
   *
   * @param pI18nID I18n key
   * @param pParams parameters for the message
   * @return matching title or "", never null
   *
   */
  public String getTitle(final String pI18nID, final Object[] pParams) {
    String key = pI18nID + SwingDecorator.TITLE_SUFFIX;
    return getString(key, pI18nID, pParams);
  }

  /**
   * Helper method that generates (if possible) a localized string for key, otherwise the I18n ID is returned.
   *
   * @param pKey the key (pI18nID+suffix)
   * @param pI18nID the key without suffix
   * @param pParams null or parameters
   * @return localized string or pI18nID
   */
  private String getString(final String pKey, final String pI18nID, final Object[] pParams) {
    if (!mMultiBundle.containsKey(pKey)) {
      if (mEnforceGetString) {
        return pI18nID;
      } else {
        return null;
      }
    }

    String value = mMultiBundle.getValue(pKey);
    if (pParams == null) {
      return value;
    } else {
      return MessageFormat.format(value, pParams);
    }
  }

  /**
   * Returns null or a Character that is the mnemonic for the handed key.
   *
   * @param pI18nID I18n key
   * @return null or a Charector object containing the mnemonic
   */
  public Character getMnemonic(final String pI18nID) {
    String key = pI18nID + SwingDecorator.MNEMONIC_SUFFIX;

    if (!mMultiBundle.containsKey(key)) {
      return null;
    }

    String value = mMultiBundle.getValue(key);
    if (value.length() > 0) {
      return value.charAt(0);
    } else {
      return null;
    }
  }

  /**
   * Returns null or a accelerator KeyStroke for the handed key.
   *
   * @param pI18nID I18n key
   * @return null or a KeyStroke containing the propper accelerator.
   */
  public KeyStroke getAccelerator(final String pI18nID) {
    String key = pI18nID + SwingDecorator.ACCELERATOR_SUFFIX;

    if (!mMultiBundle.containsKey(key)) {
      return null;
    }

    KeyStroke back = null;
    String value = mMultiBundle.getValue(key);
    if (value.length() == 1) {
      back = KeyStroke.getKeyStroke(
              "control " + Character.toUpperCase(value.charAt(0)));
    } else if ("F".equalsIgnoreCase(value.substring(0, 1))) {
      back = KeyStroke.getKeyStroke(value);
    }

    return back;
  }

  /**
   * Returns the normal Icon for the handed key or null.
   *
   * @param pI18nID I18n key
   * @return the icon, may be null
   */
  public Icon getIcon(final String pI18nID) {
    return getIcon(pI18nID, SwingDecorator.ICON_MODIFIER_NORMAL);
  }

  /**
   * Loads an Icon for the handed key and modifier. If no icon is found, null is returned.
   *
   * @param pI18nID I18n key
   * @param pIconModifier modifier for the icon name
   * @return an ImageIcon or null
   */
  public ImageIcon getIcon(final String pI18nID, final String pIconModifier) {
    return mIconCache.getIcon(pI18nID, pIconModifier);
  }

  /**
   * Decorates an action by an I18n key.
   *
   * @param pAction the action to be initialised
   * @param pI18nID I18n key
   */
  public void initialize(final Action pAction, final String pI18nID) {
    String name = getText(pI18nID);
    KeyStroke accelerator = getAccelerator(pI18nID);
    Character mnemonic = getMnemonic(pI18nID);
    String tooltip = getToolTip(pI18nID, null);
    Icon icon = getIcon(pI18nID);

    if (name != null) {
      pAction.putValue(Action.NAME, name);
    }
    if (accelerator != null) {
      pAction.putValue(Action.ACCELERATOR_KEY, accelerator);
    }
    if (mnemonic != null) {
      pAction.putValue(Action.MNEMONIC_KEY, (int) Character.toUpperCase(mnemonic));
    }
    if (tooltip != null) {
      pAction.putValue(Action.SHORT_DESCRIPTION, tooltip);
    }
    if (icon != null) {
      pAction.putValue(Action.SMALL_ICON, icon);
    }
  }

  /**
   * Decorates a widget by an I18n key.
   *
   * @param pComponent das Widget, das initialisiert werden soll
   * @param pI18nID I18n key
   */
  public void initialize(final Component pComponent, final String pI18nID) {
    initialize(pComponent, pI18nID, null);
  }

  /**
   * Decorates a widget by an I18n key.
   *
   * @param pComponent das Widget, das initialisiert werden soll
   * @param pI18nID I18n key
   * @param pParams null or parameters
   */
  public void initialize(final Component pComponent, final String pI18nID, final Object[] pParams) {
    // WARNING: the sequence of the following if/else statements if is
    //          _important_, because we must make the calls invers
    //          to the widget classes inheritence
    if (pComponent instanceof JDialog) {
      initialize((JDialog) pComponent, pI18nID, pParams);
    } else if (pComponent instanceof JFrame) {
      initialize((JFrame) pComponent, pI18nID, pParams);
    } else if (pComponent instanceof JMenuItem) {
      initialize((JMenuItem) pComponent, pI18nID, pParams);
    } else if (pComponent instanceof AbstractButton) {
      initialize((AbstractButton) pComponent, pI18nID, pParams);
    } else if (pComponent instanceof JLabel) {
      initialize((JLabel) pComponent, pI18nID, pParams);
    } else if (pComponent instanceof JTextComponent) {
      initialize((JTextComponent) pComponent, pI18nID, pParams);
    } else if (pComponent instanceof JPanel) {
      initialize((JPanel) pComponent, pI18nID, pParams);
    } else if (pComponent instanceof JComponent) {
      initialize((JComponent) pComponent, pI18nID, pParams);
    }
  }

  /**
   * Decorates the title of a JDialog widget by an I18n key.
   *
   * @param pDialog a non-null JDialog widget
   * @param pI18nID I18n key
   * @param pParams null or parameters
   *
   */
  private void initialize(final JDialog pDialog, final String pI18nID, final Object[] pParams) {
    // Title
    String title = getTitle(pI18nID, pParams);
    if (title != null) {
      pDialog.setTitle(title);
    }
  }

  /**
   * Decorates the title and icon of a JFrame widget by an I18n key.
   *
   * @param pFrame a non-null JFrame widget
   * @param pI18nID I18n key
   * @param pParams null or parameters
   */
  private void initialize(final JFrame pFrame, final String pI18nID, final Object[] pParams) {
    // Title
    String title = getTitle(pI18nID, pParams);
    if (title != null) {
      pFrame.setTitle(title);
    }

    // Icon
    ImageIcon imageIcon = getIcon(pI18nID, SwingDecorator.ICON_MODIFIER_SMALL);
    if (imageIcon != null) {
      pFrame.setIconImage(imageIcon.getImage());
    }
  }

  /**
   * Decorates the icon, text, mnemonic, tooltip and accelerator of a JMenuItem widget by an I18n key.
   *
   * @param pMenuItem a non-null JMenuItem widget
   * @param pI18nID I18n key
   * @param pParams null or parameters
   */
  private void initialize(final JMenuItem pMenuItem, final String pI18nID, final Object[] pParams) {
    // Icon
    ImageIcon imageIcon = getIcon(pI18nID, SwingDecorator.ICON_MODIFIER_SMALL);
    if (imageIcon != null) {
      pMenuItem.setIcon(imageIcon);
    }

    // Text
    String text = getText(pI18nID, pParams);
    if (text != null) {
      pMenuItem.setText(text);
    }

    // Mnemonic
    Character mnemonic = getMnemonic(pI18nID);
    if (mnemonic != null) {
      pMenuItem.setMnemonic(mnemonic);
    }

    // Tooltip
    String toolTipText = getToolTip(pI18nID, pParams);
    if (toolTipText != null && !toolTipText.equals(pI18nID)) {
      pMenuItem.setToolTipText(toolTipText);
    }

    // Accelerator
    KeyStroke accelerator = getAccelerator(pI18nID);
    if (accelerator != null) {
      pMenuItem.setAccelerator(accelerator);
    }
  }

  /**
   * Decorates the icon, text, mnemonic and tooltip of a Button derived from AbstractButton by an I18n key. Note that JMenuItem's
   * are handled elsewhere.
   *
   * @param pButton button widget
   * @param pI18nID I18n key
   * @param pParams null or parameters
   */
  private void initialize(final AbstractButton pButton, final String pI18nID, final Object[] pParams) {
    // Icon
    Icon iconNormal = getIcon(pI18nID, SwingDecorator.ICON_MODIFIER_NORMAL);
    Icon iconPressed = getIcon(pI18nID, SwingDecorator.ICON_MODIFIER_PRESSED);
    Icon iconRollover = getIcon(pI18nID, SwingDecorator.ICON_MODIFIER_ROLLOVER);
    Icon iconDisabled = getIcon(pI18nID, SwingDecorator.ICON_MODIFIER_DISABLED);
    if (iconNormal != null) {
      pButton.setIcon(iconNormal);
      pButton.setDisabledIcon(iconDisabled);
      pButton.setPressedIcon(iconPressed);
      if (iconRollover == null) {
        pButton.setRolloverEnabled(false);
      } else {
        pButton.setRolloverEnabled(true);
        pButton.setRolloverIcon(iconRollover);
      }
    }

    // Text
    String text = getText(pI18nID, pParams);
    if (text != null) {
      pButton.setText(text);
    }

    // Mnemonic
    Character mnemonic = getMnemonic(pI18nID);
    if (mnemonic != null) {
      pButton.setMnemonic(mnemonic);
    }

    // Tooltip
    String toolTipText = getToolTip(pI18nID, pParams);
    if (toolTipText != null && !toolTipText.equals(pI18nID)) {
      pButton.setToolTipText(toolTipText);
    }
  }

  /**
   * Decorates the icon, text, mnemonic and tooltip of a JLabel widget by an I18n key.
   *
   * @param pLabel label widget
   * @param pI18nID I18n key
   * @param pParams null or parameters
   */
  private void initialize(final JLabel pLabel, final String pI18nID, final Object[] pParams) {
    // Icon
    ImageIcon imageIcon = getIcon(pI18nID, SwingDecorator.ICON_MODIFIER_NORMAL);
    if (imageIcon != null) {
      pLabel.setIcon(imageIcon);
    }

    // Text
    String text = getText(pI18nID, pParams);
    if (text != null) {
      pLabel.setText(text);
    }

    // Mnemonic
    Character mnemonic = getMnemonic(pI18nID);
    if (mnemonic != null) {
      pLabel.setDisplayedMnemonic(mnemonic);
    }

    // Tooltip
    String toolTipText = getToolTip(pI18nID, pParams);
    if (toolTipText != null && !toolTipText.equals(pI18nID)) {
      pLabel.setToolTipText(toolTipText);
    }
  }

  /**
   * Decorates the text and tooltip of a text widget by an I18n key.
   *
   * @param pTextComponent text widget
   * @param pI18nID I18n key
   * @param pParams null or parameters
   */
  private void initialize(final JTextComponent pTextComponent, final String pI18nID, final Object[] pParams) {
    // Text
    String text = getText(pI18nID, pParams);
    if (text != null && !text.equals(pI18nID)) {
      pTextComponent.setText(text);
    }

    // Tooltip
    String toolTipText = getToolTip(pI18nID, pParams);
    if (toolTipText != null && !toolTipText.equals(pI18nID)) {
      pTextComponent.setToolTipText(toolTipText);
    }
  }

  /**
   * Decorates border title and tooltip of a JPanel by an I18n key.
   *
   * @param pComponent JPanel widget
   * @param pI18nID I18n key
   * @param pParams null or parameters
   */
  private void initialize(final JPanel pComponent, final String pI18nID, final Object[] pParams) {
    // Border title
    String title = getTitle(pI18nID, pParams);
    if (title != null) {
      pComponent.setBorder(new TitledBorder(title));
    }

    // Tooltip
    String toolTipText = getToolTip(pI18nID, pParams);
    if (toolTipText != null && !toolTipText.equals(pI18nID)) {
      pComponent.setToolTipText(toolTipText);
    }
  }

  /**
   * Decorates any JComponent with a tooltip by an I18n key.
   *
   * @param pComponent Swing widget
   * @param pI18nID I18n key
   * @param pParams null or parameters
   */
  private void initialize(final JComponent pComponent, final String pI18nID, final Object[] pParams) {
    // Tooltip
    String toolTipText = getToolTip(pI18nID, pParams);
    if (toolTipText != null && !toolTipText.equals(pI18nID)) {
      pComponent.setToolTipText(toolTipText);
    }
  }

  // inner classes
  /**
   * Helper class to support multiple ResourceBundles.
   */
  public static class MultiResourceBundle {

    /**
     * Map for all key/value pairs, key and value are never null
     */
    private Map<String, String> mMap = new HashMap<>();

    /**
     * Adds all non-null String key/value pairs of a bundle to the SwingDecorator singleton.
     *
     * @param pBaseName base name of the bundle to add
     */
    public void addBundle(final String pBaseName) {
      ResourceBundle bundle;
      try {
        bundle = ResourceBundle.getBundle(pBaseName);
      } catch (MissingResourceException e) {
        System.err.println("SwingDecorator.MultiResourceBundle: could not get bundle "
                + pBaseName + "\n got exception:\n" + e);
        return;
      }
      addBundle(bundle);
    }

    /**
     * Adds all non-null String key/value pairs of a bundle to the SwingDecorator singleton.
     *
     * @param pBundle the resource bundle to add
     */
    @SuppressWarnings({"ConstantConditions"})
    public void addBundle(final ResourceBundle pBundle) {
      Enumeration<String> keys = pBundle.getKeys();
      while (keys.hasMoreElements()) {
        String key = null;
        String value = null;
        try {
          key = keys.nextElement();
          value = pBundle.getString(key);
        } catch (Exception e) {
          //noinspection StringContatenationInLoop
          System.err.println("SwingDecorator.MultiResourceBundle: problems evaluating "
                  + value + "\n got (and ignored) exception:\n" + e);
        }
        if (key != null && value != null) {
          mMap.put(key, value);
        }
      }
    }

    /**
     * Gets the value string for the handed key. If the key is not known, an empty string ("") is returned.
     *
     * @param pKey key for the lookup
     * @return value for pKey, never null
     */
    public String getValue(final String pKey) {
      String value = mMap.get(pKey);
      if (value == null) {
        value = "";
      }

      return value;
    }

    /**
     * Checks if the key is known.
     *
     * @param pKey the key to check
     * @return if we know the key, false otherwise
     */
    public boolean containsKey(final String pKey) {
      return mMap.containsKey(pKey);
    }
  }
}

/**
 * ***********************************************************************
 */
