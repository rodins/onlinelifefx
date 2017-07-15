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

import java.io.File;
import java.io.IOException;

/**
 *
 * @author sergey
 */
public class PlayItemProcessor {
   
    public static Process runPlayer(PlayItem playItem) throws IOException {
        //return runSystem("mpv " + playItem.getFile());
        //return runSystem("totem " + playItem.getFile());
        //runSystem("xterm -e mplayer -cache 4096 " + link);
        return runSystem("urxvt -e mpv -cache 1028 " + playItem.getFile());
    }

    //TODO: save to video folder with proper name
    public static void runDownloader(PlayItem playItem) throws IOException {
        String sep = System.getProperty("file.separator");
        File path = new File(System.getProperty("user.home"), "Видео" + sep  + playItem.getComment());
       
        System.out.println("Path: " + path.getPath());
        runSystem("xterm -e wget -help");// -e wget");// -c " + playItem.getDownload() + " -O " + path.getPath());
    }
    //TODO: Downloader can really be changed and written in Java...

    private static Process runSystem(String command) throws IOException {
        Runtime r = Runtime.getRuntime();
        return r.exec(command); 
    }
    
}
