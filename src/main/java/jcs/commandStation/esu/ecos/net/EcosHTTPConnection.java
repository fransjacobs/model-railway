/*
 * Copyright 2024 Frans Jacobs.
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
package jcs.commandStation.esu.ecos.net;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.imageio.ImageIO;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class EcosHTTPConnection {

  private final InetAddress ecosAddress;
  private final static String HTTP = "http://";

  //http://192.168.1.110/loco/image?type=internal&index=0
  private final static String LOCOMOTIVE = "loco";
  private final static String IMAGE_FOLDER_ECOS = "/loco/image";

  EcosHTTPConnection(InetAddress csAddress) {
    this.ecosAddress = csAddress;
  }

  public boolean isConnected() {
    return ecosAddress != null && ecosAddress.getHostAddress() != null;
  }

  private static String fixURL(String url) {
    return url.replace(" ", "%20");
  }

  public Image getLocomotiveImage(String imageName) {
    BufferedImage image = null;
    try {
      URL url = URI.create(fixURL(HTTP + ecosAddress.getHostAddress() + IMAGE_FOLDER_ECOS + imageName + ".png")).toURL();

      Logger.trace("image URL: " + url);
      image = ImageIO.read(url);
    } catch (MalformedURLException ex) {
      Logger.error(ex);
    } catch (IOException ex) {
      Logger.error(ex);
    }
    return image;
  }

}
