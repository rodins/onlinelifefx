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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.concurrent.Task;

/**
 *
 * @author sergey
 */
public class ActorsTask extends Task<Actors>{
    private final String href;

    public ActorsTask(String href) {
        this.href = href;
    }

    @Override
    protected Actors call() throws Exception {
        Actors actors = new Actors();
        
        StringBuilder sb = new StringBuilder();
        URL url = new URL(href);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        InputStream stream = connection.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(stream, Charset.forName("windows-1251")));
        String s;
        while((s = in.readLine()) != null) {
            
            if(isCancelled()) {
                break;
            }
            
            // Find begining
            if(s.contains("Название:")) {
                sb.append(s).append("\n");
                continue;
            }
            
            // Add line in the middle
            if(sb.length() > 0 && !s.contains("Премьера в мире:")) {
                sb.append(s).append("\n");
                continue;
            }
            
            // Add end
            if(sb.length() > 0 && s.contains("Премьера в мире:")) {
                sb.append(s).append("\n");
                break;
            }
        }
        
        if(!sb.toString().isEmpty()) {
            Matcher m = Pattern.compile("(?s)<p>\\s+Режиссер:?.*?</p>")
                       .matcher(sb.toString());
            
            Matcher m1 = Pattern.compile("<a\\s+href=\"(.+?)\">(.+?)</a>")
                       .matcher("");
            
            if(m.find()) {
                m1.reset(m.group());
                while(m1.find()) {
                    actors.add(new Link(m1.group(2) + " (режиссёр)", m1.group(1)));
                }
            }
            
            m = Pattern.compile("(?s)<p>\\s+В ролях:?.*?</p>")
                       .matcher(sb.toString());
            
            if(m.find()) {
                m1.reset(m.group());
                while(m1.find()) {
                    // Unescape html in title
                    String title = m1.group(2);
                    title = title.replace("&#243;", "ó");
                    title = title.replace("&#233;", "é");
                    title = title.replace("&#252;", "ü");
                    actors.add(new Link(title, m1.group(1)));
                }
            }
            
            m = Pattern.compile("(?s)<p>Год:(.+?)</p>")
                    .matcher(sb.toString());
            String info = " (";
            if(m.find()) {
                info += m.group(1).trim() + ", ";
            }
            
            m = Pattern.compile("(?s)<p>\\s+Страна:(.+?)</p>")
                    .matcher(sb.toString());
            if(m.find()) {
                info += m.group(1).trim() + ")";
            }
            if(info.length() > 1) {
                actors.setInfo(info);
            }
        }
        return actors;
    }
}
