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

import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author sergey
 */
public class BackActorsListCell extends ListCell<Actors>{
    
    @Override
    protected void updateItem(Actors actors, boolean empty) {
        super.updateItem(actors, empty);
        if(empty) {
            setGraphic(null);
            setText(null);
        }else {
            setGraphic(new ImageView(
                new Image(getClass().getResourceAsStream("images/history_16.png"))));
            setText(actors.getTitle());
        }
    }
    
}
