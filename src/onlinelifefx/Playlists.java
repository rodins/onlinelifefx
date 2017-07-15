/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package onlinelifefx;

import java.util.List;

/**
 *
 * @author sergey
 */
public class Playlists {
    private String title;
    List<Playlist> playlist; // Make it accessable for json parser
    //private PlayItem playItem; // If only one item found
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getTitle() {
        return title;
    }
    
    /*public void setPlayItem(PlayItem playItem) {
        this.playItem = playItem;
    }
    
    public PlayItem getPlayItem() {
        return playItem;
    }*/
    
}
