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

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.tinylog.Logger;

/**
 * Util class to scale images
 */
public class ImageUtil {

  public static BufferedImage toBufferedImage(Image image) {
    if (image == null) {
      return null;
    }
    if (image instanceof BufferedImage bufferedImage) {
      return bufferedImage;
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

  public static Image readImage(String path) {
    Image image = null;
    File imgFile = new File(path);
    if (imgFile.exists()) {
      try {
        image = ImageIO.read(imgFile);
      } catch (IOException e) {
        Logger.trace("Image file " + path + " does not exists");
      }
    }
    return image;
  }

  public static Image scaleImage(Image image, int size) {
    Image img = image;
    if (img != null) {
      float aspect = (float) img.getHeight(null) / (float) img.getWidth(null);
      img = img.getScaledInstance(size, (int) (size * aspect), Image.SCALE_SMOOTH);
    }
    return img;
  }

  public static BufferedImage resizeImage(BufferedImage image, int targetWidth, int targetHeight) {

    if (targetWidth <= 0 || targetHeight <= 0) {
      throw new IllegalArgumentException("Target dimensions must be positive");
    }

    int currentWidth = image.getWidth();
    int currentHeight = image.getHeight();
    BufferedImage current = image;

    // Step down by at most 50% per pass.
    // For 400→40 (10x): 400→200→100→50→40  (4 passes)
    // Each pass preserves thin lines that a single 10x pass destroys.
    while (currentWidth > targetWidth * 2 || currentHeight > targetHeight * 2) {
      currentWidth = Math.max(currentWidth / 2, targetWidth);
      currentHeight = Math.max(currentHeight / 2, targetHeight);
      current = scaleStep(current, currentWidth, currentHeight);
    }

    // Final pass to exact target dimensions (may be a non-power-of-two step)
    if (currentWidth != targetWidth || currentHeight != targetHeight) {
      current = scaleStep(current, targetWidth, targetHeight);
    }

    return current;
  }

  public static BufferedImage resizeImageTwoPass(BufferedImage image, int targetWidth, int targetHeight) {

    if (targetWidth <= 0 || targetHeight <= 0) {
      throw new IllegalArgumentException("Target dimensions must be positive");
    }

    int sourceWidth = image.getWidth();
    int sourceHeight = image.getHeight();

    if (sourceWidth == targetWidth && sourceHeight == targetHeight) {
      return image;
    }

    // For small reductions or upscaling,
    // a single pass is sufficient.
    if (sourceWidth <= targetWidth * 3 && sourceHeight <= targetHeight * 3) {
      return scaleStep(image, targetWidth, targetHeight);
    }

    // Intermediate size: approximately 3x the final size.
    int intermediateWidth = Math.min(sourceWidth, targetWidth * 3);
    int intermediateHeight = Math.min(sourceHeight, targetHeight * 3);

    BufferedImage intermediate = scaleStep(image, intermediateWidth, intermediateHeight);

    BufferedImage result = scaleStep(intermediate, targetWidth, targetHeight);
    intermediate.flush();
    return result;
  }

  private static BufferedImage scaleStep(BufferedImage src, int w, int h) {
    int type = src.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
    BufferedImage dest = new BufferedImage(w, h, type);

    Graphics2D g = dest.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.drawImage(src, 0, 0, w, h, null);
    g.dispose();
    return dest;
  }

}
