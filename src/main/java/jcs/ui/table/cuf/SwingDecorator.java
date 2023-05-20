package jcs.ui.table.cuf;

import jcs.util.IconCache;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import java.awt.Component;
import java.util.ResourceBundle;

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
public class SwingDecorator {

  /**
   * Suffix for JFrame and JDialog titles
   */
  public static final String TITLE_SUFFIX = ".title";

  /**
   * Suffix for text
   */
  public static final String TEXT_SUFFIX = ".text";

  /**
   * Suffic for Mnemonics
   */
  public static final String MNEMONIC_SUFFIX = ".mnemonic";

  /**
   * Suffix for Accelerators
   */
  public static final String ACCELERATOR_SUFFIX = ".accelerator";

  /**
   * Suffix for Tooltips
   */
  public static final String TOOLTIP_SUFFIX = ".tooltip";

  /**
   * Suffix for icon (file) names
   */
  public static final String ICONNAME_SUFFIX = ".iconname";

  /**
   * Prefix for the the icon files
   */
  public static final String ICON_PREFIX = IconCache.ICON_PREFIX;
  /**
   * Suffix1 for the the icon files
   */
  public static final String ICON_SUFFIX1 = IconCache.ICON_SUFFIX1;
  /**
   * Suffix2 for the the icon files
   */
  public static final String ICON_SUFFIX2 = IconCache.ICON_SUFFIX2;
  /**
   * Suffix for the normal icon
   */
  public static final String ICON_MODIFIER_NORMAL = IconCache.ICON_MODIFIER_NORMAL;
  /**
   * Suffix for the disabled icon
   */
  public static final String ICON_MODIFIER_DISABLED = IconCache.ICON_MODIFIER_DISABLED;
  /**
   * Suffix for the pressed icon
   */
  public static final String ICON_MODIFIER_PRESSED = IconCache.ICON_MODIFIER_PRESSED;
  /**
   * Suffix for the rollover icon
   */
  public static final String ICON_MODIFIER_ROLLOVER = IconCache.ICON_MODIFIER_ROLLOVER;
  /**
   * Suffix for the small icon
   */
  public static final String ICON_MODIFIER_SMALL = IconCache.ICON_MODIFIER_SMALL;

  /**
   * Our singleton instance.
   */
  private static SwingDecoratorFunctionality sManager = new SwingDecoratorFunctionality();

  /**
   * We have a private constructor, because SwingDecorator is modeled as a singleton wrapper around
   * {@link SwingDecoratorFunctionality}.
   */
  private SwingDecorator() {
  }

  /**
   * Adds a property file to SwingDecorator, see ResourceBundle.getBundle() for the details how the property file is found.
   *
   * @param pBaseName base name of the resource bundle
   */
  public static void addBundle(final String pBaseName) {
    sManager.addBundle(pBaseName);
  }

  /**
   * Adds all non-null String key/value pairs of a bundle to the SwingDecorator singleton.
   *
   * @param pResourceBundle the resource bundle to add, must not be null
   */
  public static void addBundle(final ResourceBundle pResourceBundle) {
    sManager.addBundle(pResourceBundle);
  }

  /**
   * Returns the I18n text matching the handed key. If there is no value for the key, the key is returned.
   *
   * @param pI18nID I18n key
   * @return matching localized string or "", never null
   */
  public static String getText(final String pI18nID) {
    return sManager.getText(pI18nID);
  }

  /**
   * Returns the I18n text matching the handed key and parameters. If there is no value for the key, the key is returned. See
   * MessageFormat.format for the formatting details.
   *
   * @param pI18nID I18n key
   * @param pParams parameters for the message
   * @return matching localized string or "", never null
   */
  public static String getText(final String pI18nID, final Object[] pParams) {
    return sManager.getText(pI18nID, pParams);
  }

  /**
   * Returns the I18n tool tip matching the handed key and parameters. If there is no value for the key, the key is returned. See
   * MessageFormat.format for the formatting details.
   *
   * @param pI18nID I18n key
   * @param pParams parameters for the message
   * @return matching tool tip or pI18nID, never null
   */
  public static String getToolTip(final String pI18nID, final Object[] pParams) {
    return sManager.getToolTip(pI18nID, pParams);
  }

  /**
   * Returns the I18n title matching the handed key. If there is no value for the key, the key is returned.
   *
   * @param pI18nID I18n key
   * @return matching title or pI18nID, never null
   */
  public static String getTitle(final String pI18nID) {
    return sManager.getTitle(pI18nID);
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
  public static String getTitle(final String pI18nID, final Object[] pParams) {
    return sManager.getText(pI18nID, pParams);
  }

  /**
   * Returns null or a Character that is the mnemonic for the handed key.
   *
   * @param pI18nID I18n key
   * @return null or a Character object containing the mnemonic
   */
  public static Character getMnemonic(final String pI18nID) {
    return sManager.getMnemonic(pI18nID);
  }

  /**
   * Returns null or a accelerator KeyStroke for the handed key.
   *
   * @param pI18nID I18n key
   * @return null or a KeyStroke containing the propper accelerator.
   */
  public static KeyStroke getAccelerator(final String pI18nID) {
    return sManager.getAccelerator(pI18nID);
  }

  /**
   * Returns the normal Icon for the handed key or null.
   *
   * @param pI18nID I18n key
   * @return the icon, may be null
   */
  public static Icon getIcon(final String pI18nID) {
    return sManager.getIcon(pI18nID);
  }

  /**
   * Loads an Icon for the handed key and modifier. If no icon is found, null is returned.
   *
   * @param pI18nID I18n key
   * @param pIconModifier modifier for the icon name
   * @return an ImageIcon or null
   */
  public static ImageIcon getIcon(final String pI18nID, final String pIconModifier) {
    return sManager.getIcon(pI18nID, pIconModifier);
  }

  /**
   * Decorates a widget by an I18n key.
   *
   * @param pComponent das Widget, das initialisiert werden soll
   * @param pI18nID I18n key
   */
  public static void initialize(final Component pComponent, final String pI18nID) {
    sManager.initialize(pComponent, pI18nID);
  }

  /**
   * Decorates a widget by an I18n key.
   *
   * @param pComponent das Widget, das initialisiert werden soll
   * @param pI18nID I18n key
   * @param pParams null or parameters
   */
  public static void initialize(final Component pComponent, final String pI18nID, final Object[] pParams) {
    sManager.initialize(pComponent, pI18nID, pParams);
  }
}
