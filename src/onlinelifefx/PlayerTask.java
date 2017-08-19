/*
 * Copyright (C) 2017 1
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
import java.io.InputStreamReader;
import javafx.concurrent.Task;

/**
 *
 * @author 1
 */
public class PlayerTask extends Task<Void> {
    private String link;
    
    public PlayerTask(String link) {
        this.link = link;
    }
    
    @Override
    protected Void call() throws Exception {
        String os = System.getProperty("os.name");
        String player = "";
        if(os.equals("Linux")) {
            player = "mpv";
        }else if(os.equals("Windows")) {
            // TODO: Show file dialog to find player
            // TODO: Save path to player as property
            // TODO: On windows test JavaFX media player
            player = "D:\\sergey\\mpv\\mpv.exe";
        }

        ProcessBuilder pb = new ProcessBuilder(player, link);
        Process proc = pb.start();
        // Need to read stardart error for process not to hang
        BufferedReader stdError = new BufferedReader(
                new InputStreamReader(proc.getErrorStream()));
        while (stdError.readLine() != null) {
            // Do not really need output, only need to empty buffer
        }
        return null;
    }
    
}
