/*
 * Copyright (C) 2018 Frans Jacobs.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package lan.wervel.jcs.trackservice;

import java.lang.reflect.InvocationTargetException;
import org.pmw.tinylog.Logger;

/**
 *
 * @author frans
 */
public class TrackServiceFactory {

    private TrackService trackService;
    private static TrackServiceFactory instance;

    private TrackServiceFactory() {
    }

    public static TrackServiceFactory getInstance() {
        if (instance == null) {
            instance = new TrackServiceFactory();
            instance.aquireTrackServiceImpl();
        }
        return instance;
    }

    public static TrackService getTrackService() {
        return TrackServiceFactory.getInstance().getTrackServiceImpl();
    }

    private TrackService getTrackServiceImpl() {
        return trackService;
    }

    private boolean aquireTrackServiceImpl() {
        try {
            TrackService ts = (TrackService) Class.forName("lan.wervel.jcs.trackservice.H2TrackService").getDeclaredConstructor().newInstance();
            this.trackService = ts;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException ex2) {
            Logger.error("Can't instantiate a 'lan.wervel.jcs.trackservice.H2TrackService' " + ex2.getMessage());
        }
        Logger.debug("Using " + trackService.getClass().getSimpleName() + " as Track Service...");
        return trackService != null;
    }
}
