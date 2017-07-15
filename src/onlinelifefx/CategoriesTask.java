/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package onlinelifefx;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import javafx.concurrent.Task;

/**
 *
 * @author sergey
 */
public class CategoriesTask extends Task<List<Link>> {
    private final String DOMAIN;
    
    public CategoriesTask(String addr) {
        DOMAIN = addr;
    } 
    
    @Override
    protected List<Link> call() throws Exception {
        URL url = new URL(DOMAIN);
        HttpURLConnection connection = null;
        BufferedReader in = null; 
        try {
            connection = (HttpURLConnection)url.openConnection();
            InputStream stream = connection.getInputStream();
            in = new BufferedReader(new InputStreamReader(stream, Charset.forName("windows-1251")));
            String html = CategoriesParser.getCategoriesPart(in, this);
            return CategoriesParser.parseCategories(html);
        }finally {
            if(in != null) {
                in.close();
            }
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

}
