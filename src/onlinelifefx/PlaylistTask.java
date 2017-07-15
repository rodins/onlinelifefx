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

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.concurrent.Task;

/**
 *
 * @author sergey
 */
public class PlaylistTask extends Task<Playlists> {
    private final String js;
    private final int BUFFER_SIZE = 1440; 

    public PlaylistTask(String js) {
        this.js = js;
    }
    
    private String getHttpPage(String addr) throws Exception {
        //COMPLETED: switch to try with resources
        StringBuilder sb = new StringBuilder();
        URL url = new URL(addr);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try ( InputStream stream = connection.getInputStream();
              BufferedReader in = new BufferedReader(new InputStreamReader(stream)); 
        ) {
            char[] buffer = new char[BUFFER_SIZE];
            int length;
            while((length = in.read(buffer)) != -1) {
                if(isCancelled()) {
                    break;
                }
                sb.append(buffer, 0, length);
            }
            return sb.toString();
            
        }finally {
            connection.disconnect();
        }
    }

    @Override
    protected Playlists call() throws Exception {
        Playlists playlists;
        Matcher m = Pattern.compile("\"pl\":\"(.+?)\"").matcher(js);
        if(m.find()) {
            String json = getHttpPage(m.group(1));
            Gson gson = new Gson();
            playlists = gson.fromJson(json, Playlists.class);
            if(!playlists.playlist.isEmpty()) {
                if(playlists.playlist.get(0).playlist != null) {
                    return playlists;
                }else {
                    Playlist playlist = gson.fromJson(json, Playlist.class);
                    playlists.playlist.clear();
                    playlists.playlist.add(playlist);
                    return playlists;
                }
            }
        }
        return null;
    }
}
