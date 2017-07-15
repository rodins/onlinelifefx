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

import java.util.Map;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import org.controlsfx.control.GridCell;

/**
 *
 * @author sergey
 */
public class ResultCell extends GridCell<Result> {
    
    private final Map<String, Image> imagesCache;
    
    public ResultCell(Map<String, Image> iCache) {
        imagesCache = iCache;
        setFont(new Font("Tahoma", 12));
        setWrapText(true);
        setStyle("-fx-background-color: #ffffff;"
                    + " -fx-background-radius: 15;"
                    + " -fx-border-radius: 15;"
                    + " -fx-border-width: 0;"
                    + " -fx-padding: 10;"
                    + " -fx-effect: dropshadow(three-pass-box, #93948d, 10, 0, 0, 0);");
    }

    @Override
    protected void updateItem(Result result, boolean empty) {
        super.updateItem(result, empty);
        
        if(empty || result == null) {
            setText(null);
            setGraphic(null);
        }else {
            setContentDisplay(ContentDisplay.TOP);
            ImageView imageView;
            if(imagesCache.containsKey(result.ImageLink)) {
                imageView = new ImageView(imagesCache.get(result.ImageLink));
            }else {
                Image image = new Image(result.ImageLink, true);
                imagesCache.put(result.ImageLink, image);
                imageView = new ImageView(image);
            }
            setText(result.Title);
            setGraphic(imageView);
        }
    }
    
}
