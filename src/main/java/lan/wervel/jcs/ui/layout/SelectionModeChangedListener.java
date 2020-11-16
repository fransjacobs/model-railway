/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lan.wervel.jcs.ui.layout;

import lan.wervel.jcs.ui.layout.tiles.enums.Direction;
import lan.wervel.jcs.ui.layout.tiles.enums.Rotation;

/**
 *
 * @author frans
 */
public interface SelectionModeChangedListener {

    void selectionModeChanged(Mode newMode, Rotation newOrientation, Direction newDirection, TileType newTileType);

}
