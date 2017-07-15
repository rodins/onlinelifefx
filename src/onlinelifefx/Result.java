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

import java.io.Serializable;

/**
 *
 * @author sergey
 */
public class Result implements Serializable {
    private static final long serialVersionUID = 1L;
    public final String Title;
    public final String Href;
    public final String Id;
    public final String ImageLink;
  
    public Result(String title, String href, String id, String imageLink) {
        Title = title;
        Href = href;
        Id = id;
        ImageLink = imageLink + "&w=165&h=236&zc=1";//"&w=82&h=118&zc=1";
        //"&w=165&h=236&zc=1";
    }
    
    @Override
    public String toString() {
        return Title;
    }
}
