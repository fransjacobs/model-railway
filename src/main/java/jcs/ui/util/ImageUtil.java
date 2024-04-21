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

  public static BufferedImage getBufferdImage(Image image) {
    GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice graphicsDevice = graphicsEnvironment.getDefaultScreenDevice();
    GraphicsConfiguration graphicsConfiguration = graphicsDevice.getDefaultConfiguration();

    BufferedImage bufferedImage = graphicsConfiguration.createCompatibleImage(image.getWidth(null), image.getHeight(null), Transparency.BITMASK);
    Graphics graphics = bufferedImage.createGraphics();
    graphics.drawImage(image, 0, 0, null);
    graphics.dispose();
    return bufferedImage;
  }

  private static BufferedImage rotate180(BufferedImage bufferedImage) {
    AffineTransform tx = AffineTransform.getScaleInstance(-1, -1);
    tx.translate(-bufferedImage.getWidth(null), -bufferedImage.getHeight(null));
    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    return op.filter(bufferedImage, null);
  }

  public static BufferedImage flipVertically(Image sourceImage) {
    BufferedImage bufferedImage = getBufferdImage(sourceImage);
    AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
    tx.translate(0, -bufferedImage.getHeight(null));
    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    bufferedImage = op.filter(bufferedImage, null);
    return rotate180(bufferedImage);
  }

  public static BufferedImage flipHorizontally(Image sourceImage) {
    BufferedImage bufferedImage = getBufferdImage(sourceImage);
    AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
    tx.translate(-bufferedImage.getWidth(null), 0);
    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    bufferedImage = op.filter(bufferedImage, null);
    return rotate180(bufferedImage);
  }

//  public static BufferedImage rotate(Image sourceImage, int angle) {
//    BufferedImage bufferedImage = getBufferdImage(sourceImage);
//
//    double theta = (Math.PI * 2) / 360 * angle;
//    int width = bufferedImage.getWidth();
//    int height = bufferedImage.getHeight();
//    BufferedImage dest;
//    if (angle == 90 || angle == 270) {
//      //dest = new BufferedImage(bufferedImage.getHeight(), bufferedImage.getWidth(), bufferedImage.getType());
//      dest = new BufferedImage(bufferedImage.getHeight(), bufferedImage.getWidth(), BufferedImage.TYPE_INT_ARGB_PRE);
//    } else {
//      dest = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getType());
//    }
//
//    Graphics2D graphics2D = dest.createGraphics();
//
//    switch (angle) {
//      case 90 -> {
//        graphics2D.translate((height - width) / 2, (height - width) / 2);
//        graphics2D.rotate(theta, height / 2, width / 2);
//      }
//      case 270 -> {
//        graphics2D.translate((width - height) / 2, (width - height) / 2);
//        graphics2D.rotate(theta, height / 2, width / 2);
//      }
//      default -> {
//        graphics2D.translate(0, 0);
//        graphics2D.rotate(theta, width / 2, height / 2);
//      }
//    }
//    graphics2D.drawRenderedImage(bufferedImage, null);
//    return dest;
//  }
//  
//  final double rads = Math.toRadians(90);
//final double sin = Math.abs(Math.sin(rads));
//final double cos = Math.abs(Math.cos(rads));
//final int w = (int) Math.floor(image.getWidth() * cos + image.getHeight() * sin);
//final int h = (int) Math.floor(image.getHeight() * cos + image.getWidth() * sin);
//final BufferedImage rotatedImage = new BufferedImage(w, h, image.getType());
//final AffineTransform at = new AffineTransform();
//at.translate(w / 2, h / 2);
//at.rotate(rads,0, 0);
//at.translate(-image.getWidth() / 2, -image.getHeight() / 2);
//final AffineTransformOp rotateOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
//rotateOp.filter(image,rotatedImage);
//https://coderanch.com/t/638786/java/Image-rotation-position  
//  
  public static BufferedImage rotate(Image image, int angle) {
    BufferedImage img = getBufferdImage(image);

    AffineTransform transform = new AffineTransform();
    transform.rotate(Math.toRadians(angle), img.getWidth(null) / 2, img.getHeight(null) / 2);
    AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
    img = op.filter(img, null);

    return img;
  }

//  public static BufferedImage rotate(Image image, int angle) {
//    BufferedImage img = getBufferdImage(image);
//    // Getting Dimensions of image
//    int width = img.getWidth();
//    int height = img.getHeight();
//    
//
//    // Creating a new buffered image
//    BufferedImage newImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
//
//    // creating Graphics in buffered image
//    Graphics2D g2 = newImage.createGraphics();
//
//    // Rotating image by degrees using toradians()
//    // method
//    // and setting new dimension t it
//    g2.rotate(Math.toRadians(angle), width / 2, height / 2);
//    g2.drawImage(img, null, 0, 0);
//
//    // Return rotated buffer image
//    return newImage;
//  }
  /**
   * Rotates an Image object. Taken from http://stackoverflow.com/a/4156760/2159348.
   *
   * @param image the image to rotate
   * @param angle rotation in radians
   * @return rotated image
   */
  public static Image rotate1(Image image, double a) {
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

  /**
   * Converts an image to a buffered image.
   *
   * @param image the image to be converted
   * @return buffered image
   */
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

}
