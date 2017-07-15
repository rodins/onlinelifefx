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
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import javafx.concurrent.Task;

/**
 *
 * @author sergey
 */
class DownloadTask extends Task<Void>{
    private final String link;
    private final File saveFile;
    private long fileLength, prevFileSize;
    private int percent;
    
    public DownloadTask(String link, File saveFile) {
        this.link = link;
        this.saveFile = saveFile;
    }

    @Override
    protected Void call() throws Exception {
        URL url = new URL(link);
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection)url.openConnection();
            
            if(saveFile.exists()){
                conn.setAllowUserInteraction(true);
                conn.setRequestProperty("Range", "bytes=" + saveFile.length() + "-");
            }
            
            conn.setConnectTimeout(14000);
            conn.setReadTimeout(20000);
            conn.connect();
            
            //Check for free space
            long webFileSize = conn.getContentLength();
            fileLength = webFileSize + saveFile.length();
            long freeSpace = saveFile.getParentFile().getFreeSpace();
            if(freeSpace < fileLength){
                throw new Exception("Not enough space!");
            }
            // Using nio and try with resources
            try (
                ReadableByteChannel in = Channels.newChannel(conn.getInputStream());
                FileChannel out = new FileOutputStream(saveFile, saveFile.exists()).getChannel();
            ){
                percent = (int)(saveFile.length()*100/fileLength);
                final int interval = 5;
                
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        long diff = saveFile.length() - prevFileSize;
                        if(diff > 0) {
                            long speed = diff/1024/interval;
                            long countedTime = interval * (fileLength-saveFile.length())/diff;
                            prevFileSize = saveFile.length();
                            updateMessage(toMb(saveFile.length()) + " Mb from " 
                                + toMb(fileLength) + " Mb, " + percent + "%, "
                                + "Time left: " + getTime(countedTime) 
                                + ", Speed: " + speed + " kB/s");
                        }else {
                            updateMessage(toMb(saveFile.length()) + "Mb from " 
                                + toMb(fileLength) + "Mb, " + percent + "%. "
                                + "Staled...");
                        }
                    }
                
                }, interval*1000, interval*1000);
                prevFileSize = saveFile.length();
                while(out.transferFrom(in, saveFile.length(), 1440) > 0){
                    if(isCancelled()) {
                        break;
                    }
                    percent = (int)(saveFile.length()*100/fileLength);
                    updateProgress(saveFile.length(), fileLength);
                }
            }
        }finally {
            if(conn != null) {
                conn.disconnect();
            }
        }
        
        // Using io
        /*try {
            conn = (HttpURLConnection)url.openConnection();
            
            if(saveFile.exists()){
                conn.setAllowUserInteraction(true);
                conn.setRequestProperty("Range", "bytes=" + saveFileSize + "-");
            }
            conn.setConnectTimeout(14000);
            conn.setReadTimeout(20000);
            conn.connect();

            long webFileSize = conn.getContentLength();
            if(webFileSize == -1) {
                throw new Exception("Network problem!");
            }

            if(conn.getResponseCode()/100 != 2) { 
                throw new Exception("Network problem!");
            }


            String connectionRangeField = conn.getHeaderField("content-range");
            //Resume download is supported
            if(connectionRangeField != null){
                //Resuming download...
                System.out.println("Resuming download...");
            }

            //Resume download is not supported 
            // Delete existing file
            if(connectionRangeField == null && saveFile.exists()){
                    saveFile.delete();
                    saveFileSize = 0;
            }

            long fileLength = webFileSize + saveFileSize;

            //Check for free space
            long freeSpace = saveFile.getParentFile().getFreeSpace();
            if(freeSpace < fileLength){
                    throw new Exception("Not enough space!");
            }
           
            
            BufferedInputStream in = null; 
            RandomAccessFile raf = null;
            try {
                in = new BufferedInputStream(conn.getInputStream());
                raf = new RandomAccessFile(saveFile, "rw");

                raf.seek(saveFileSize);
                final byte[] buffer = new byte[1024];
                int count, percent = 0, prevPercent;
                while((count = in.read(buffer, 0, buffer.length)) != -1){
                    if (isCancelled()) {
                        break;
                    }
                    raf.write(buffer, 0, count);
                    saveFileSize += count;
                    prevPercent = percent;
                    percent = (int)(saveFileSize*100/fileLength);
                    if(percent > prevPercent) {
                        updateMessage("(" + toMb(saveFileSize) + "Mb from " 
                                + toMb(fileLength) + "Mb, " + percent + "%)");
                    }
                    updateProgress(saveFileSize, fileLength);
                }
            }finally {
                if(in != null) {
                    in.close();
                }
                if(raf != null) {
                    raf.close();
                }
            }
        }finally {
            if(conn != null) {
                conn.disconnect();
            }
        }*/
        
        return null;
    }
    
    private long toMb(long bytes) {
        return bytes/1024/1024;
    }
    
    private String getTime(long prognosedTime){
        long minutes = TimeUnit.MINUTES.convert(prognosedTime, TimeUnit.SECONDS);
        if(minutes == 0){
            long seconds = prognosedTime;
            return seconds + " sec.";
        }else if(minutes/60 == 0){
            return minutes + " min.";
        }else if(minutes/60 > 0){
            int hours = (int)minutes/60;
            int hrMinutes = (int)(minutes - hours*60);
            return hours + " h. " + hrMinutes + " min.";
        }
        return "Unknown";
    }
    
}
