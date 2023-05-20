package jcs.util;

import javax.swing.ImageIcon;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;

/**
 * The icon cache is a class that loads icons and stores them in a Map. The next time the same icon is needed, it's taken from the
 * cache. If a icon can't be loaded on the first try, no further access are tried.
 */
public class IconCache {

  /*
     * icon stuff taken from SwingDecorater, should always be in sync
   */
  /**
   * Suffix for the the icon files
   */
  public static final String ICON_PREFIX = "icons/";
  /**
   * first suffix for the the icon files
   */
  public static final String ICON_SUFFIX1 = ".png";
  /**
   * second suffix for the the icon files
   */
  public static final String ICON_SUFFIX2 = ".gif";
  /**
   * Suffix for the normal icon
   */
  public static final String ICON_MODIFIER_NORMAL = "";
  /**
   * Suffix for the disabled icon
   */
  public static final String ICON_MODIFIER_DISABLED = "Disabled";
  /**
   * Suffix for the pressed icon
   */
  public static final String ICON_MODIFIER_PRESSED = "Pressed";
  /**
   * Suffix for the selected icon
   */
  public static final String ICON_MODIFIER_SELECTED = "Selected";
  /**
   * Suffix for the disabled selected icon
   */
  public static final String ICON_MODIFIER_DISABLED_SELECTED = "DisabledSelected";
  /**
   * Suffix for the rollover icon
   */
  public static final String ICON_MODIFIER_ROLLOVER = "Rollover";
  /**
   * Suffix for the selected rollover icon
   */
  public static final String ICON_MODIFIER_ROLLOVER_SELECTED = "RolloverSelected";
  /**
   * Suffix for the small icon
   */
  public static final String ICON_MODIFIER_SMALL = "Small";

  /**
   * cache for our icons
   */
  private Map<String, ImageIcon> mIconCache = new HashMap<>();

  /**
   * Loads an Icon for the handed (relative) name and modifier. If no icon is found, null is returned.
   *
   * @param pName relative name, may be null
   * @param pIconModifier modifier for the icon name
   * @return an ImageIcon or null
   */
  public ImageIcon getIcon(final String pName, final String pIconModifier) {
    if (pName == null) {
      return null;
    }

    String name1 = "/" + ICON_PREFIX + pName + pIconModifier + ICON_SUFFIX1;
    String name2 = "/" + ICON_PREFIX + pName + pIconModifier + ICON_SUFFIX2;

    if (mIconCache.containsKey(name1)) {
      return mIconCache.get(name1);
    }
    if (mIconCache.containsKey(name2)) {
      return mIconCache.get(name2);
    }

    ImageIcon imageIcon = null;
    try {
      URL url = getClass().getResource(name1);
      imageIcon = new ImageIcon(url);
      imageIcon.getImage();
      mIconCache.put(name1, imageIcon);
    } catch (Exception ignored1) {
      try {
        URL url = getClass().getResource(name2);
        imageIcon = new ImageIcon(url);
        imageIcon.getImage();
        mIconCache.put(name2, imageIcon);
      } catch (Exception ignored2) {
        mIconCache.put(name1, null);
        mIconCache.put(name2, imageIcon);
        // we ignore load problems, and will not try to load the
        // same icon again
      }
    }

    return imageIcon;
  }
}
