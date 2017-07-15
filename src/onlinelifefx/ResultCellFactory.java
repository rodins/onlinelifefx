/*
 * Copyright (C) 2017 sergey
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package onlinelifefx;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;

/**
 *
 * @author sergey
 */
public class ResultCellFactory implements Callback<GridView<Result>, GridCell<Result>> {
    private final Map<String, Image> imagesCache = new HashMap<>();
    private final OnlinelifeFX gui;
    
    public ResultCellFactory(OnlinelifeFX gui) {
        this.gui = gui;
    }
    
    @Override
    public GridCell<Result> call(GridView<Result> param) {
        ResultCell resultCell = new ResultCell(imagesCache);
        
        resultCell.setOnMouseClicked((MouseEvent event) -> {
            /*if(event.getClickCount() == 2) {
                gui.resultClicked(resultCell.getItem());
            }else if(event.getClickCount() == 1) {
                gui.actorsAction(resultCell.getItem());
            }*/
            if(event.getClickCount() == 1) {
                gui.resultClicked(resultCell.getItem());
            }
        });
        
        return resultCell;
    }
    
}
