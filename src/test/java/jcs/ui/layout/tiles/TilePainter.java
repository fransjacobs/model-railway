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
package jcs.ui.layout.tiles;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.Timer;
import org.tinylog.Logger;

public class TilePainter extends JPanel implements Scrollable {

    private static final long serialVersionUID = 1L;
    private final int TILE_SIZE = 50;
    private final int TILE_COUNT = 100;
    private final int visibleTiles = 10;
    private final boolean[][] loaded;
    private final boolean[][] loading;
    private final Random random;

    public TilePainter() {
        setPreferredSize(new Dimension(TILE_SIZE * TILE_COUNT, TILE_SIZE * TILE_COUNT));
        loaded = new boolean[TILE_COUNT][TILE_COUNT];
        loading = new boolean[TILE_COUNT][TILE_COUNT];
        random = new Random();
    }

    public boolean getTile(final int x, final int y) {
        boolean canPaint = loaded[x][y];
        if (!canPaint && !loading[x][y]) {
            loading[x][y] = true;
            Timer timer = new Timer(random.nextInt(500),
                    new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            loaded[x][y] = true;
                            repaint(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        }
                    });
            timer.setRepeats(false);
            timer.start();
        }
        return canPaint;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Rectangle clip = g.getClipBounds();
        
        Logger.trace("R clip: ("+clip.x+","+clip.y+") [w: "+clip.width+", h: "+clip.height+"]");
        
        int startX = clip.x - (clip.x % TILE_SIZE);
        int startY = clip.y - (clip.y % TILE_SIZE);
        
        Logger.trace("Start: ("+startX+","+startY+")");
        
        for (int x = startX; x < clip.x + clip.width; x += TILE_SIZE) {
            for (int y = startY; y < clip.y + clip.height; y += TILE_SIZE) {
                if (getTile(x / TILE_SIZE, y / TILE_SIZE)) {
                    g.setColor(Color.GREEN);
                } else {
                    g.setColor(Color.RED);
                }
                g.fillRect(x, y, TILE_SIZE - 1, TILE_SIZE - 1);
            }
        }
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(visibleTiles * TILE_SIZE, visibleTiles * TILE_SIZE);
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return TILE_SIZE * Math.max(1, visibleTiles - 1);
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return TILE_SIZE;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                JFrame frame = new JFrame("Tiles");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.getContentPane().add(new JScrollPane(new TilePainter()));
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }
}