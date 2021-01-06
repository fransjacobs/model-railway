/*
 * Copyright (C) 2021 frans.
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
package lan.wervel.jcs.ui.layout;

import java.util.HashSet;
import java.util.Set;
import lan.wervel.jcs.entities.LayoutTile;
import lan.wervel.jcs.trackservice.TrackServiceFactory;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Logger;

/**
 *
 * @author frans
 */
public class TrackAnalyser {

    private final Set<LayoutTile> tiles;

    public TrackAnalyser() {
        this.tiles = new HashSet<>();
    }

    public void loadLayout() {
        tiles.clear();
        Set<LayoutTile> layoutTiles = TrackServiceFactory.getTrackService().getLayoutTiles();
        tiles.addAll(layoutTiles);
        Logger.trace("Loaded " + tiles.size() + " (layout)Tiles...");
        
        Set<LayoutTile> blocks = new HashSet<>();
        for(LayoutTile lt : tiles) {
            if(TileType.BLOCK.getTileType().equals(lt.getTiletype())) {
                blocks.add(lt);
                Logger.trace("Added Block: "+lt);
            }
        }
        Logger.trace("Found "+blocks.size()+" blocks...");
        
        //eerst blokke ananlyseren en alle layouttile group zetten
        //links en recht of boven en ondert analyseren todat je een turnout tegen komt
        
        //Need the block

       TrackServiceFactory.getTrackService().disconnect();
       System.exit(0);
    }

    public static void main(String args[]) {
        Configurator.defaultConfig().level(org.pmw.tinylog.Level.TRACE).activate();
        
        
        TrackAnalyser ta = new TrackAnalyser();
        
        ta.loadLayout();
        

    }

}
