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
package org.xhtmlrenderer.demo.browser;

import org.w3c.dom.Document;
import org.xhtmlrenderer.util.Uu;

import java.net.URL;
import java.util.ArrayList;


/**
 * The history manager keeps track of all of the documents that have been
 * loaded, and the order in which they have been loaded (the page navigation
 * trail). This allows the browser to go forwards and backwards through the
 * list. It caches the root document object of each page, along with the
 * location URL. There is one history manager in memory per browser view (ie,
 * per window or tab), since each view has it's own navigation trail.
 *
 * @author empty
 */

public class HistoryManager {
    /**
     * Description of the Field
     */
    protected ArrayList entries = new ArrayList();
    /**
     * Description of the Field
     */
    protected int index = -1;

    /**
     * Description of the Method
     */
    public void goPrevious() {
        index--;
    }

    /**
     * Description of the Method
     *
     * @return Returns
     */
    public boolean hasPrevious() {
        if (index > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Description of the Method
     *
     * @return Returns
     */
    public boolean hasNext() {
        if (index + 1 < entries.size() && index >= 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Description of the Method
     */
    public void goNext() {
        index++;
    }

    /**
     * Description of the Method
     *
     * @param doc PARAM
     */
    public void goNewDocument(Document doc) {
        goNewDocument(doc, null);
    }

    /**
     * Description of the Method
     *
     * @param doc PARAM
     * @param url PARAM
     */
    public void goNewDocument(Document doc, URL url) {
        //Uu.p("going to a new document: " + doc + " " + url);
        //Uu.dump_stack();
        Entry entry = new Entry();
        entry.doc = doc;
        entry.url = url;
        // clear out array list after index

        int len = entries.size();
        for (int i = index + 1; i < len; i++) {
            entries.remove(index + 1);
        }

        entries.add(entry);
        index++;
        //dumpHistory();
    }

    /**
     * Description of the Method
     */
    public void dumpHistory() {
        Uu.p("history:");
        Uu.p(entries);
        Uu.p("current index = " + index);
    }

    /**
     * Gets the currentDocument attribute of the HistoryManager object
     *
     * @return The currentDocument value
     */
    public Document getCurrentDocument() {
        return getEntry(index).doc;
    }

    /**
     * Gets the currentURL attribute of the HistoryManager object
     *
     * @return The currentURL value
     */
    public URL getCurrentURL() {
        return getEntry(index).url;
    }

    /**
     * Gets the entry attribute of the HistoryManager object
     *
     * @param i PARAM
     * @return The entry value
     */
    protected Entry getEntry(int i) {
        return (Entry) entries.get(i);
    }

    /**
     * Description of the Class
     *
     * @author empty
     */
    protected class Entry {
        /**
         * Description of the Field
         */
        public Document doc;
        /**
         * Description of the Field
         */
        public URL url;

        /**
         * Converts to a String representation of the object.
         *
         * @return A string representation of the object.
         */
        public String toString() {
            return "HistoryEntry: " + doc;
        }
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.6  2004/12/12 03:33:07  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.5  2004/12/12 02:55:29  tobega
 * Making progress
 *
 * Revision 1.4  2004/11/17 00:44:54  joshy
 * fixed bug in the history manager
 * added cursor support to the link listener
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/10/23 14:38:58  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */

