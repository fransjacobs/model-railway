/*
 * Copyright 2023 Frans Jacobs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcs.persistence;

import java.lang.reflect.InvocationTargetException;
import jcs.persistence.util.H2DatabaseUtil;
import jcs.util.RunUtil;
import org.tinylog.Logger;

public class PersistenceFactory {

    private PersistenceService persistenceService;
    private static PersistenceFactory instance;

    private PersistenceFactory() {
    }

    private static PersistenceFactory getInstance() {
        if (instance == null) {
            instance = new PersistenceFactory();
            instance.createPersistenceService(false);
        }
        return instance;
    }

    public static PersistenceService getService() {
        return PersistenceFactory.getInstance().getPersistenceServiceImpl();
    }

    private PersistenceService getPersistenceServiceImpl() {
        return persistenceService;
    }

    protected boolean createPersistenceService(boolean test) {
        String persistenceServiceImpl = System.getProperty("persistenceService");

        if (persistenceServiceImpl == null) {
            RunUtil.loadProperties();
            persistenceServiceImpl = System.getProperty("persistenceService");
        }

        H2DatabaseUtil.setProperties(test);

        if (persistenceServiceImpl != null) {
            try {
                this.persistenceService = (PersistenceService) Class.forName(persistenceServiceImpl).getDeclaredConstructor().newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                Logger.error("Can't instantiate a '" + persistenceServiceImpl + "' " + ex.getMessage());
                Logger.trace(ex);
            }
        } else {
            Logger.error("Can't find implementation class for property: 'persistenceService'!");
        }

        if (persistenceService != null) {
            Logger.debug("Using " + persistenceService.getClass().getSimpleName() + " as PersistenceService...");
        }
        return persistenceService != null;
    }

}
