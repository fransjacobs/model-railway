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
package lan.wervel.marklin.track.monitor;

/**
 *
 * @author frans
 */
public class TrackMonitorFactory {

  private final ShadowStationImpl shadowStation;

  private static TrackMonitorFactory trackMonitorFactory;

  private TrackMonitorFactory() {
    //For now there is only one implementation
    shadowStation = new ShadowStationImpl();
  }

  public static TrackMonitorFactory getInstance() {
    if (trackMonitorFactory == null) {
      trackMonitorFactory = new TrackMonitorFactory();
    }
    return trackMonitorFactory;
  }

  public static TrackMonitor getTrackMonitor() {
    return TrackMonitorFactory.getInstance().getshadowStation();
  }

  /**
   *
   * @return
   */
  public ShadowStationImpl getshadowStation() {
    return shadowStation;
  }

}
