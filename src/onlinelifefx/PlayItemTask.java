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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import javafx.concurrent.Task;

/**
 *
 * @author sergey
 */
public class PlayItemTask extends Task<PlayItem> {
    private final Result result;
    private final String referer = "http://dterod.com/player.php";
    private final int BUFFER_SIZE = 1440;
    
    public PlayItemTask(Result result) {
        this.result = result;
    }
    
    private String getJsVar() throws Exception {
        String addr = "http://dterod.com/js.php?id=" + result.Id;
        StringBuilder sb = new StringBuilder();
        URL url = new URL(addr);
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("Referer", referer);
            try (
                InputStream in = connection.getInputStream();
                BufferedReader buff = new BufferedReader(new InputStreamReader(in,
                                                             Charset.forName("windows-1251")));
            ) {
                char[] buffer = new char[BUFFER_SIZE];
                int length;
                while((length = buff.read(buffer)) != -1) {
                    if(isCancelled()) {
                        break;
                    }
                    sb.append(buffer, 0, length);
                }
                return sb.toString();
            }
        }finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }
    
    private String getTrailerJs() throws Exception {
        URL url = new URL(result.Href);
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection)url.openConnection();
            try(
                BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), Charset.forName("windows-1251")));
            ) {
                String s;
                while((s = in.readLine()) != null) {
                    if(isCancelled()) {
                        break;
                    }

                    if(s.contains("dterod.com")) {
                        String query = "trailer_id=";
                        int begin = s.indexOf(query);
                        int end = s.indexOf("\'", begin);
                        String trailerId = s.substring(begin + query.length(), end);

                        url = new URL("http://dterod.com/js.php?id=" + trailerId + "trailer&trailer=1");
                        connection.disconnect();
                        connection = (HttpURLConnection)url.openConnection();
                        connection.setRequestProperty("Referer", referer);
                        try(
                            BufferedReader in2 = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        ) {
                            StringBuilder sb = new StringBuilder();
                            while((s = in2.readLine()) != null) {
                                sb.append(s).append("\n");
                            }
                            return sb.toString();
                        }
                    }
                }
            }
            
        }finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
        return "";
    }

    @Override
    protected PlayItem call() throws Exception {
        String js = getJsVar();
        if(js.trim().isEmpty()) {
            js = getTrailerJs();
        }
        PlayItem playItem = new PlayItem(js);
        return playItem;
    }
    
}
