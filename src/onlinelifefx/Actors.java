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

import java.util.Objects;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author sergey
 */
class Actors {
    private String title;
    private String info;
    private final ObservableList<Link> items;
    
    public Actors() {
        items = FXCollections.observableArrayList();
    }
    
    public Actors(String title, String info, ObservableList<Link> items) {
        this.title = title;
        this.info = info;
        this.items = items;
    }
    
    public void add(Link link) {
        items.add(link);
    }
    
    public ObservableList<Link> getItems() {
        return items;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setInfo(String info) {
        this.info = info;
    }
    
    public String getInfo() {
        return info;
    }
    
    @Override
    public String toString() {
        return title;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Actors) {
            Actors actors = (Actors)obj;
            return title.equals(actors.getTitle());
        }else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.title);
        return hash;
    }
}
