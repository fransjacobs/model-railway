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
package lan.wervel.jcs.repository;

import lan.wervel.jcs.common.TrackRepository;

/**
 *
 * @author frans
 */
public class RepositoryFactory {

  private final TrackRepository trackRepository;

  private static RepositoryFactory trackRepositoryFactory;

  private RepositoryFactory() {
    //For now there is only one implementation
    trackRepository = new XmlRepository();
  }

  public static RepositoryFactory getInstance() {
    if (trackRepositoryFactory == null) {
      trackRepositoryFactory = new RepositoryFactory();
    }
    return trackRepositoryFactory;
  }

  public static TrackRepository getRepository() {
    return RepositoryFactory.getInstance().getTrackRepository();
  }

  public TrackRepository getTrackRepository() {
    return trackRepository;
  }

}
