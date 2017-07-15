/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package onlinelifefx;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author sergey
 */
public class PlayItem {
    private String comment;
    private String file;
    private String download;
    private String js;
    private int count = 0;
    
    public PlayItem() {
        comment = file = download = js = "";
    }
    
    public PlayItem(String comment, String file, String download) {
        this.comment = comment;
        this.file = file;
        this.download = download;
    }
    
    public PlayItem(String js) {
        this.js = js;
        Matcher m = Pattern.compile("\"(file|download|comment)\":\"(.+?)\"").matcher(js);
        while(m.find()) {
            switch(m.group(1)) {
                case "file":
                    file = m.group(2);
                    break;
                case "download":
                    download = m.group(2);
                    break;
                case "comment":
                    comment = m.group(2);
                    break;     
            }
            count++;
        }
    }
    
    public String getJs() {
        return js;
    }
    
    public boolean isJsPlayItem() {
        return count == 3; // if js contained PlayItem
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public String getComment() { return comment; }

    public void setFile(String file) {
        this.file = file;
    }
    
    public String getFile() { return file; }

    public void setDownload(String download) {
        this.download = download;
    }
    
    public String getDownload() { return download; }
    
    @Override
    public String toString() {
        return comment;
    }
}
