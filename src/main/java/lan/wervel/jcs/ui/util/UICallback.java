/*
 * Copyright (C) 2019 frans.
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
package lan.wervel.jcs.ui.util;

import java.io.File;
import java.util.List;

/**
 *
 * @author frans
 */
public interface UICallback {

  /**
   *
   * @param files the files selected
   */
  void openFiles(List<File> files);

  /**
   *
   * @return true when the application can quit
   */
  boolean handleQuitRequest();

  /**
   * Show About dialog
   */
  void handleAbout();

  /**
   * Show preferences dialog
   */
  void handlePreferences();

}
