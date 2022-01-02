/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lan.wervel.jcs.ui.layout;

import jcs.ui.layout2.enums.Mode;
import jcs.entities.enums.TileType;
import jcs.ui.layout.tiles.enums.Direction;
import jcs.entities.enums.Orientation;

/**
 *
 * @author frans
 */
public interface SelectionModeChangedListener {

    void selectionModeChanged(Mode newMode, Orientation newOrientation, Direction newDirection, TileType newTileType);

}
