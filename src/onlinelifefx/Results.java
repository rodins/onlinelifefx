/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package onlinelifefx;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author sergey
 */
public class Results {
    private String title;
    private final List<Result> items = new ArrayList<>();
    private String prevLink = "", nextLink = "", currentPage = "";
    private List<Link> categories;
    private boolean isSave = true;
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getPageTitle() {
        if(!currentPage.isEmpty()) {
            return title + " - Page: " + currentPage;
        }else {
            return title;
        }
    }
    
    public String getTitle() {
        return title;
    }
    
    public void add(Result result) {
        items.add(result);
    }
    
    public List<Result> getItems() {
        return items;
    }
    
    public void setPrevLink(String prev) {
        prevLink = prev;
    }
    
    public void setNextLink(String next) {
        nextLink = next;
    }
    
    public String getPrevLink() {
        return prevLink;
    }
    
    public String getNextLink() {
        return nextLink;
    }
    
    public void setCurrentPage(String page) {
        currentPage = page;
    }
    
    public String getCurrentPage() {
        return currentPage;
    }
    
    public void setCategories(List<Link> c) {
        categories = c;
    }
    
    public List<Link> getCategories() {
        return categories;
    }
    
    @Override
    public String toString() {
        return getPageTitle();
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Results) {
            Results results = (Results)obj;
            return this.getPageTitle().equals(results.getPageTitle());
        }else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.title);
        return hash;
    }
    
    public void setSaveable(boolean save) {
        isSave = save;
    }
    
    public boolean isSaveable() {
        return isSave;
    }
    
}
