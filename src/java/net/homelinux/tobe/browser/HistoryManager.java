
/* 
 * {{{ header & license 
 * Copyright (c) 2004 Joshua Marinacci 
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; either version 2.1 
 * of the License, or (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. 
 * }}} 
 */

package net.homelinux.tobe.browser;

import org.xhtmlrenderer.util.u;
import java.net.URI;
//import org.w3c.dom.Document;
import net.homelinux.tobe.renderer.XRDocument;
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
        public XRDocument doc;
        public URI uri;
        public String toString() {
            return "HistoryEntry: " + doc;
        }
    }
    
    protected Entry getEntry(int i) {
        return (Entry)entries.get(i);
    }
    
    public XRDocument getCurrentDocument() {
        return getEntry(index).doc;
    }
    
    public URI getCurrentURI() {
        return getEntry(index).uri;
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
    public void goNewDocument(XRDocument doc) {
        goNewDocument(doc,null);
    }
    public void goNewDocument(XRDocument doc, URI uri) {
        Entry entry = new Entry();
        entry.doc = doc;
        entry.uri = uri;
        // clear out array list after index
        
        int len = entries.size();
        for(int i=index+1; i<len; i++) {
            entries.remove(index+1);
        }
        
        entries.add(entry);
        index++;
        //dumpHistory();
    }
    
    public boolean isVisited(URI uri) {
        for(java.util.Iterator i = entries.iterator(); i.hasNext();) {
            Entry e = (Entry) i.next();
            if(uri.equals(e.uri)) return true;
        }
        return false;
    }
    
    public void dumpHistory() {
        u.p(entries);
        u.p("current index = " + index);
    }

}
