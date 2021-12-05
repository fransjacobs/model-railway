/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lan.wervel.jcs.ui.layout;

import java.util.Set;
import lan.wervel.jcs.ui.layout.tiles.AbstractTile;

/**
 *
 * @author frans
 */
public interface SelectionListener {

  void setSelectedLayoutTiles(Set<AbstractTile> tiles);

}
