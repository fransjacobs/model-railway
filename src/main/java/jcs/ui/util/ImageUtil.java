/*
 * Copyright 2024 fransjacobs.
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
package jcs.ui.util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

/**
 *
 * @author fransjacobs
 */
public class ImageUtil {

  public static BufferedImage toBufferedImage1(Image image) {
    GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice graphicsDevice = graphicsEnvironment.getDefaultScreenDevice();
    GraphicsConfiguration graphicsConfiguration = graphicsDevice.getDefaultConfiguration();

    BufferedImage bufferedImage = graphicsConfiguration.createCompatibleImage(image.getWidth(null), image.getHeight(null), Transparency.BITMASK);
    Graphics graphics = bufferedImage.createGraphics();
    graphics.drawImage(image, 0, 0, null);
    graphics.dispose();
    return bufferedImage;
  }

  public static BufferedImage toBufferedImage(Image image) {
    if (image instanceof BufferedImage) {
      return (BufferedImage) image;
    }

    BufferedImage buff = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = buff.createGraphics();
    g.drawImage(image, 0, 0, null);
    g.dispose();

    return buff;
  }

  private static BufferedImage rotate180(BufferedImage bufferedImage) {
    AffineTransform tx = AffineTransform.getScaleInstance(-1, -1);
    tx.translate(-bufferedImage.getWidth(null), -bufferedImage.getHeight(null));
    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    return op.filter(bufferedImage, null);
  }

  public static BufferedImage flipVertically(Image sourceImage) {
    BufferedImage bufferedImage = toBufferedImage(sourceImage);
    AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
    tx.translate(0, -bufferedImage.getHeight(null));
    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    bufferedImage = op.filter(bufferedImage, null);
    return rotate180(bufferedImage);
  }

  public static BufferedImage flipHorizontally(Image sourceImage) {
    BufferedImage bufferedImage = toBufferedImage(sourceImage);
    AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
    tx.translate(-bufferedImage.getWidth(null), 0);
    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    bufferedImage = op.filter(bufferedImage, null);
    return rotate180(bufferedImage);
  }

  public static Image rotate(Image image, double a) {
    BufferedImage bufImg = toBufferedImage(image);
    double angle = (Math.PI * 2) / 360 * a;

    double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
    int w = bufImg.getWidth(), h = bufImg.getHeight();
    int neww = (int) Math.floor(w * cos + h * sin), newh = (int) Math.floor(h * cos + w * sin);
    BufferedImage result = new BufferedImage(neww, newh, Transparency.TRANSLUCENT);
    Graphics2D g = result.createGraphics();
    g.translate((neww - w) / 2, (newh - h) / 2);
    g.rotate(angle, w / 2, h / 2);
    g.drawRenderedImage(bufImg, null);
    g.dispose();
    return result;
  }

}
