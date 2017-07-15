/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package onlinelifefx;

import java.io.BufferedReader;
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
public class ResultsTask extends Task<Results> {
    private final String link;
    private final String baseUrl;
    private final boolean isCategories;
        
    public ResultsTask(String link, String baseUrl, boolean isCategories) {
        this.link = link;
        this.baseUrl = baseUrl;
        this.isCategories = isCategories;
    }
    
    // Only get part of web page needed to be parsed
    private String getResultsPart(BufferedReader in) throws Exception{
        StringBuilder sb = new StringBuilder();
        String s;
        while((s = in.readLine()) != null) {
            
            if(isCancelled()) {
                break;
            }
            
            // Find begining
            if(sb.length() == 0 && s.contains("custom-poster")) {
                sb.append(s).append("\n");
                continue;
            }
            
            // Add line in the middle
            if(sb.length() > 0 && !s.contains("</table>")) {
                sb.append(s).append("\n");
                continue;
            }
            
            // Add end
            if(sb.length() > 0 && s.contains("</table>")) {
                sb.append(s).append("\n");
                return sb.toString();
            }
            
        }
        return null;
    }
    
    private Results parseResults(String html) {
        Results results = new Results();
        // Get items
        if(html != null && !html.isEmpty()) {
            Matcher m = Pattern
                    .compile("<a\\s+href=\"(http://www.online-life.[a-z]+?/(\\d+?)-.*?html)\"\\s*?>\\n\\s*<img\\s+src=\"(.*?)\"\\s+/>(.+?)\\n?\\s*</a>")
                    .matcher(html);
            while(m.find()) {
               /* System.out.println(m.group(1)); //href
                System.out.println(m.group(2)); //ID
                System.out.println(m.group(3)); //image
                System.out.println(m.group(4)); //title*/
                String imageLink = m.group(3);
                imageLink = imageLink.substring(0, imageLink.indexOf("&"));
                results.add(new Result(m.group(4), m.group(1), m.group(2), imageLink));
            }
            
            // Parse pager;
            m = Pattern
                    .compile("<div class=\"navigation\" align=\"center\" >.*?</div>")
                    .matcher(html);
            if(m.find()) {
                // Get current page
                Matcher m1 = Pattern.compile("<span>(.+?)</span>").matcher(m.group());
                while(m1.find()) {
                    if(m1.group(1).length() < 5) {
                        results.setCurrentPage(m1.group(1));
                    }
                }

                // Non-search page navigation links
                Matcher m2 = Pattern.compile("<a\\s+href=\"(.+?)\">(.+?)</a>").matcher(m.group());
                while(m2.find()) {
                    if(m2.group(2).length() == 6) { //Next
                        results.setNextLink(m2.group(1));
                    }
                    if(m2.group(2).length() == 5) { //Prev
                        results.setPrevLink(m2.group(1));
                    }
                }

                // Search page navigation links
                Matcher m3 = Pattern.compile("<a.+?onclick=\".+?(\\d+).+?\">(.+?)</a>").matcher(m.group());
                while(m3.find()) {
                    if(m3.group(2).length() == 6) { //Next
                        results.setNextLink(baseUrl + "&search_start=" + m3.group(1));
                    }
                    if(m3.group(2).length() == 5) { //Prev
                        results.setPrevLink(baseUrl + "&search_start=" + m3.group(1));
                    }
                }
            }
        }
        
        return results;
    }

    @Override
    protected Results call() throws Exception {
        URL url = new URL(link);
        HttpURLConnection connection = null; 
        try {
            connection = (HttpURLConnection)url.openConnection();
            try(
                 BufferedReader in  = new BufferedReader(new InputStreamReader(
                         connection.getInputStream(), Charset.forName("windows-1251")));
            ) {
                String html1 = null;
                if(isCategories) {
                    html1 = CategoriesParser.getCategoriesPart(in, this);
                }

                String html2 = getResultsPart(in);
                Results results = parseResults(html2);
                if(isCategories) {
                    results.setCategories(CategoriesParser.parseCategories(html1));
                }
                
                return results;
            }
        }finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }
}
