/*
 * Copyright (C) 2022 frans.
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
package jcs.controller.cs3.http;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.h2.util.IOUtils;
import org.json.JSONObject;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class SvgIconToPngIconConverter {

    private static final String SVG_NAME_SPACE = "<svg version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"";
    private static final String YELLOW = "<style>.st0{fill:rgb(220, 160, 10);}</style>";

    public SvgIconToPngIconConverter() {
        ImageIO.scanForPlugins();
    }

    private void convertAndCacheFunctionImage(String imageName, String svg) {
        String path = System.getProperty("user.home") + File.separator + "jcs" + File.separator + "cache" + File.separator + "functions";
        File cachePath = new File(path);
        if (cachePath.mkdir()) {
            Logger.trace("Created new directory " + cachePath);
        }

        try {
            String svgBlack = svg.replaceFirst("<svg", SVG_NAME_SPACE);
            String svg1 = svgBlack.substring(0, svgBlack.indexOf("<path"));
            String svg2 = svgBlack.substring(svgBlack.indexOf("<path"));
            String svgYellow = svg1 + YELLOW + svg2;

            String name1 = imageName.substring(0, imageName.lastIndexOf("_"));
            String name2 = imageName.substring(imageName.lastIndexOf("_"));

            String imageNameBK = name1 + "_sw" + name2;
            String imageNameYE = name1 + "_ge" + name2;

            BufferedImage imgBlack = ImageIO.read(IOUtils.getInputStreamFromString(svgBlack));
            ImageIO.write((BufferedImage) imgBlack, "PNG", new File(path + File.separator + imageNameBK + ".png"));

            BufferedImage imgYellow = ImageIO.read(IOUtils.getInputStreamFromString(svgYellow));
            ImageIO.write((BufferedImage) imgYellow, "PNG", new File(path + File.separator + imageNameYE + ".png"));
        } catch (IOException ex) {
            Logger.error("Can't store image " + cachePath.getName() + "! ", ex.getMessage());
        }
    }

    private void handleJSONObject(JSONObject jsonObject) {
        for (String key : jsonObject.keySet()) {
            String svg = jsonObject.getString(key);
            String imageBaseName = key.substring(0, key.indexOf("."));
            convertAndCacheFunctionImage(imageBaseName, svg);
        }
    }

    public void convertAndCacheAllFunctionsSvgIcons(String json) {
        JSONObject jo = new JSONObject(json);
        handleJSONObject(jo);
    }

}
