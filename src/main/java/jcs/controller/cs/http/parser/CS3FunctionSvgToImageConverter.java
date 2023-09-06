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
package jcs.controller.cs.http.parser;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.h2.util.IOUtils;
import org.json.JSONObject;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class CS3FunctionSvgToImageConverter {

  private static final String SVG_NAME_SPACE = "<svg version=\"1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"";

  private static final String YELLOW = "<style>.st0{fill:rgb(220, 160, 10);}</style>";
  private static final String WHITE = "<style>.st0{fill:rgb(255, 255, 255);}</style>";
  private static final String GREY = "<style>.st0{fill:rgb(102, 102, 102);}</style>";
  private static final String BLACK = "<style>.st0{fill:rgb(0, 0, 0);}</style>";

  private final Map<String, String> functionSvgCache;

  public CS3FunctionSvgToImageConverter() {
    ImageIO.scanForPlugins();
    functionSvgCache = new HashMap<>();
  }
  private String getSvg(String imageName) {
    //Remove te color
    String svgName = imageName.replaceFirst("_ge_", "_");
    svgName = svgName.replaceFirst("_we_", "_");
    svgName = svgName.replaceFirst("_gr_", "_");
    svgName = svgName.toLowerCase();
    return this.functionSvgCache.get(svgName);
  }

  public BufferedImage getFunctionImage(String imageName) {
    String svg = getSvg(imageName);
    //Get the color
    String color;
    if (imageName.contains("_ge_")) {
      color = YELLOW;
    } else if (imageName.contains("we")) {
      color = WHITE;
    } else if (imageName.contains("we")) {
      color = GREY;
    } else {
      color = BLACK;
    }

    String svgBase = svg;
    if (!svg.contains(SVG_NAME_SPACE)) {
      svgBase = svgBase.replaceFirst("<svg", SVG_NAME_SPACE);
      //Logger.trace(svgBase);
    }

    String svg1 = svgBase.substring(0, svgBase.indexOf("<path"));
    String svg2 = svgBase.substring(svgBase.indexOf("<path"));

    String svgStyle = svg1 + color + svg2;
    //Logger.trace(svgStyle);

    if (svgStyle.contains("fill=\"")) {
      svgStyle = svgStyle.replaceAll("fill=\"(.*?)\"", "fill=\"#DCA00A\"");
    } else {
      svgStyle = svgStyle.replaceAll("<path ", "<path fill=\"rgb(220, 160, 10)\" ");
    }

    BufferedImage img = null;
    try {
      img = ImageIO.read(IOUtils.getInputStreamFromString(svgStyle));
    } catch (IOException ex) {
      Logger.error("Can't convert " + imageName, ex);
    }
    return img;
  }

  public void loadSvgCache(String json) {
    this.functionSvgCache.clear();
    JSONObject jsonObject = new JSONObject(json);
    for (String key : jsonObject.keySet()) {
      String svg = jsonObject.getString(key);
      String svgName = key.substring(0, key.indexOf("."));
      this.functionSvgCache.put(svgName, svg);
    }
    Logger.trace("Loaded " + this.functionSvgCache.size() + " svg images");
  }


}
