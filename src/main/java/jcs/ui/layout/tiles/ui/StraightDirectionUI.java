/*
 * Copyright 2025 Frans Jacobs.
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
package jcs.ui.layout.tiles.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

public class StraightDirectionUI extends StraightUI {

  public StraightDirectionUI() {
  }

  public static ComponentUI createUI(JComponent c) {
    return new StraightDirectionUI();
  }

  private void renderDirectionArrow(Graphics2D g2, JComponent c) {
    //   |\
    // ==|+===
    //   |/
    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(Color.green.darker());

    g2.fillPolygon(new int[]{150, 150, 270}, new int[]{130, 270, 200}, 3);
  }

  @Override
  public void renderTile(Graphics2D g2, JComponent c) {
    renderStraight(g2, c);
    renderDirectionArrow(g2, c);
  }

}
