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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

    private static final String SVG_NAME_SPACE = "<svg version=\"1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"";
    private static final String YELLOW = "<style>.st0{fill:rgb(220, 160, 10);}</style>";

    public SvgIconToPngIconConverter() {
        ImageIO.scanForPlugins();
    }

    private void convertAndCacheFunctionImage(String imageName, String svg) throws IOException {
        convertAndCacheFunctionImage(imageName, svg, false);
    }

    void convertAndCacheFunctionImage(String imageName, String svg, boolean testMode) throws IOException {
        String path = System.getProperty("user.home") + File.separator + "jcs" + File.separator + "cache" + File.separator + "functions";
        File cachePath = new File(path);
        if (cachePath.mkdir()) {
            Logger.trace("Created new directory " + cachePath);
        }

        //try {
        if (testMode) {
            String svgp = System.getProperty("user.home") + File.separator + "jcs" + File.separator + "cache" + File.separator + "svg";
            File svgPath = new File(svgp);
            if (svgPath.mkdir()) {
                Logger.trace("Created new directory " + svgPath);
            }

            //For debug also write the svg's to a file            
            File svgf = new File(svgp + File.separator + imageName + ".svg");
            try ( BufferedWriter writer = new BufferedWriter(new FileWriter(svgf))) {
                writer.write(svg);
            }
        }

        if (1 == 1) {
            String svgBlack = svg;

            if (!svg.contains(SVG_NAME_SPACE)) {
                svgBlack = svgBlack.replaceFirst("<svg", SVG_NAME_SPACE);
                Logger.trace(svgBlack);
            }

            String svg1 = svgBlack.substring(0, svgBlack.indexOf("<path"));
            String svg2 = svgBlack.substring(svgBlack.indexOf("<path"));

            //Add Yellow style
            String svgYellow = svg1 + YELLOW + svg2;

            Logger.trace(svgYellow);
            if (svgYellow.contains("fill=\"")) {
                svgYellow = svgYellow.replaceAll("fill=\"(.*?)\"", "fill=\"#DCA00A\"");
            } else {
                svgYellow = svgYellow.replaceAll("<path ", "<path fill=\"rgb(220, 160, 10)\" ");
            }

            String name1 = imageName.substring(0, imageName.lastIndexOf("_"));
            String name2 = imageName.substring(imageName.lastIndexOf("_"));

            String imageNameBK = name1 + "_sw" + name2;
            String imageNameYE = name1 + "_ge" + name2;

            BufferedImage imgBlack = ImageIO.read(IOUtils.getInputStreamFromString(svgBlack));
            ImageIO.write((BufferedImage) imgBlack, "PNG", new File(path + File.separator + imageNameBK + ".png"));

            BufferedImage imgYellow = ImageIO.read(IOUtils.getInputStreamFromString(svgYellow));
            ImageIO.write((BufferedImage) imgYellow, "PNG", new File(path + File.separator + imageNameYE + ".png"));
        }
    }

    private void handleJSONObject(JSONObject jsonObject) {
        for (String key : jsonObject.keySet()) {
            String svg = jsonObject.getString(key);
            String imageBaseName = key.substring(0, key.indexOf("."));
            try {
                convertAndCacheFunctionImage(imageBaseName, svg);
            } catch (IOException e) {
                Logger.error("Error in image " + imageBaseName + ": " + e.getMessage());
            }
        }
        Logger.info("Processed " + jsonObject.keySet().size() + " svg's");
    }

    public void convertAndCacheAllFunctionsSvgIcons(String json) {
        JSONObject jo = new JSONObject(json);
        handleJSONObject(jo);
    }
}
