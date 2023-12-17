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
package jcs.commandStation.marklin.cs3;

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
public class FunctionSvgToPngConverter {

  private static final String SVG_NAME_SPACE = "<svg version=\"1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"";

  private static final String YELLOW = "<style>.st0{fill:rgb(220, 160, 10);}</style>";
  private static final String WHITE = "<style>.st0{fill:rgb(255, 255, 255);}</style>";
  private static final String GREY = "<style>.st0{fill:rgb(102, 102, 102);}</style>";
  private static final String BLACK = "<style>.st0{fill:rgb(0, 0, 0);}</style>";

  private static final Map<String, String> functionSvgCache = new HashMap<>();

  static {
    ImageIO.scanForPlugins();
  }

//  public FunctionSvgToPngConverter() {
//    ImageIO.scanForPlugins();
//  }
  private static String getCS3SvgName(String imageName) {
    //Remove te color
    //TODO prepend a 0 or 2 0 when number is only 1 or 2 long
    String svgName = imageName.replaceFirst("_ge_", "_");
    svgName = svgName.replaceFirst("_we_", "_");
    svgName = svgName.replaceFirst("_gr_", "_");
    svgName = svgName.toLowerCase();
    return functionSvgCache.get(svgName);
  }

  public static BufferedImage getFunctionImageCS2(String imageName) {
    return getFunctionImageCS3(getCS3SvgName(imageName));
  }

  public static BufferedImage getFunctionImageCS3(String imageName) {
    String svgName = imageName.replace("_we_", "_").replace("_ge_", "_");
    String svg = getCS3SvgName(svgName);

    if (svg == null) {
      Logger.trace("ImageName " + imageName + "; '" + svgName + "' not found");
      return null;
    }

    //Get the color
    String color;
    String col;
    if (imageName.contains("_ge_")) {
      color = YELLOW;
      col = "ge";
    } else if (imageName.contains("_we_")) {
      color = WHITE;
      col = "we";
    } else if (imageName.contains("_gr_")) {
      color = GREY;
      col = "gr";
    } else {
      color = BLACK;
      col = "bk";
    }

    String rep, rep1;
    switch (col) {
      case "ge" -> {
        rep = "fill=\"#DCA00A\"";
        rep1 = "<path fill=\"rgb(220, 160, 10)\" ";
      }
      case "we" -> {
        rep = "fill=\"#FFFFFF\"";
        rep1 = "<path fill=\"rgb(255, 255, 255)\" ";
      }
      case "gr" -> {
        rep = "fill=\"#666666\"";
        rep1 = "<path fill=\"rgb(102, 102, 102)\" ";
      }
      default -> {
        rep = "fill=\"#000000\"";
        rep1 = "<path fill=\"rgb(0, 0, 0)\" ";
      }
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

      svgStyle = svgStyle.replaceAll("fill=\"(.*?)\"", rep);
    } else {
      svgStyle = svgStyle.replaceAll("<path ", rep1);
    }

    BufferedImage img = null;
    try {
      img = ImageIO.read(IOUtils.getInputStreamFromString(svgStyle));
    } catch (IOException ex) {
      Logger.error("Can't convert " + imageName, ex);
    }
    return img;
  }

  public static void loadSvgCache(String json) {
    functionSvgCache.clear();
    JSONObject jsonObject = new JSONObject(json);
    for (String key : jsonObject.keySet()) {
      String svg = jsonObject.getString(key);
      String svgName = key.substring(0, key.indexOf("."));
      functionSvgCache.put(svgName, svg);
    }
    Logger.trace("Loaded " + functionSvgCache.size() + " svg images");
  }
  
  public static void clearSvgCache() {
    functionSvgCache.clear();
  }

  public static boolean isSvgCacheLoaded() {
    return !functionSvgCache.isEmpty();
  }

}
