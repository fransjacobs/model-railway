package jcs.ui.table.cuf;

import jcs.ui.table.cuf.DefaultWidgetFactory;
import java.awt.Container;
import java.util.HashMap;
import java.util.Map;

import org.jdom2.Element;

/**
 * Registry is to be used for the registration of Widget Factories with the <code>SwingXMLBuilder</code>, also a WidgetFactory.
 */
public class WidgetFactoryRegistry implements WidgetFactory {

  /**
   * Key for a factory that is to be used for the creation of all types of widgets.
   */
  public static final String ALL_TYPES = "allTypes";

  /**
   * Map containing all registered factories, never null.
   */
  private Map<String, WidgetFactory> mFactoryMap;

  /*
     * WidgetFactory stuff
   */
  public Container create(final String pType, final Element pElement, final Container pParent) {
    return findValidFactory(pType).create(pType, pElement, pParent);
  }

  public void notifyWidgetComplete(final String pType, final Element pElement, final Container pCurrentWidget) {
    findValidFactory(pType).notifyWidgetComplete(pType, pElement, pCurrentWidget);
  }

  /*
     * registry stuff
   */
  /**
   * This package scoped constructor should only be called by SwingXMLBuilder.
   */
  WidgetFactoryRegistry() {
    mFactoryMap = new HashMap<>();

    // set the default factory
    registerFactory(ALL_TYPES, new DefaultWidgetFactory());
  }

  /**
   * Registers a Factory with the given type
   *
   * @param pType the type the factory is to register with
   * @param pFactory the factory to be used for the creation of widgets for the given type
   * @throws IllegalArgumentException if pType="allTypes" and pFactory is null
   */
  public void registerFactory(final String pType, final WidgetFactory pFactory) throws IllegalArgumentException {
    if (ALL_TYPES.equals(pType) && (pFactory == null)) {
      throw new IllegalArgumentException("default factory (allTypes) must not be null");
    }
    mFactoryMap.put(pType, pFactory);
  }

  /**
   * Finds a valid factory for the respective type. The following mechanism is used: 1. Try to find a factory that is specifically
   * registered for the given type. 2. Try to find a factory that is registered for all types. If both operations fail, an exception
   * is thrown indicating we don't have a registered factory for the given type.
   *
   * @param pType the type the factory should create
   * @return a valid factory for the respective type, never null
   * @throws IllegalArgumentException if there is no factory for the given type
   */
  private WidgetFactory findValidFactory(final String pType) {
    WidgetFactory myFactory = mFactoryMap.get(pType);
    if (myFactory == null) {
      myFactory = mFactoryMap.get(ALL_TYPES);
      if (myFactory == null) {
        throw new IllegalArgumentException("No Registry found for type "
                + pType.getClass().getName());
      }
    }
    return myFactory;
  }
}
