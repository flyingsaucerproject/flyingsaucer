package org.joshy.html.app.browser;

import org.joshy.u;
import java.net.URL;
import org.w3c.dom.Document;
import java.util.ArrayList;

/**

The history manager keeps track of all of the documents that have been
loaded, and the order in which they have been loaded (the page navigation
trail). This allows the browser to go forwards and backwards through the
list. It caches the root document object of each page, along with the
location URL. There is one history manager in memory per browser view (ie,
per window or tab), since each view has it's own navigation trail.

*/


public class HistoryManager {
    protected ArrayList entries = new ArrayList();
    protected int index = -1;
    
    protected class Entry {
        public Document doc;
        public URL url;
        public String toString() {
            return "HistoryEntry: " + doc;
        }
    }
    
    protected Entry getEntry(int i) {
        return (Entry)entries.get(i);
    }
    
    public Document getCurrentDocument() {
        return getEntry(index).doc;
    }
    
    public URL getCurrentURL() {
        return getEntry(index).url;
    }
    
    public void goPrevious() {
        index--;
    }
    public boolean hasPrevious() {
        if(index > 0) { 
            return true;
        } else {
            return false;
        }
    }
    
    public boolean hasNext() {
        if(index+1 < entries.size() && index >= 0) {
            return true;
        } else {
            return false;
        }
    }
    public void goNext() {
        index++;
    }
    public void goNewDocument(Document doc) {
        goNewDocument(doc,null);
    }
    public void goNewDocument(Document doc, URL url) {
        Entry entry = new Entry();
        entry.doc = doc;
        entry.url = url;
        // clear out array list after index
        
        int len = entries.size();
        for(int i=index+1; i<len; i++) {
            entries.remove(index+1);
        }
        
        entries.add(entry);
        index++;
        //dumpHistory();
    }
    
    public void dumpHistory() {
        u.p(entries);
        u.p("current index = " + index);
    }

}
