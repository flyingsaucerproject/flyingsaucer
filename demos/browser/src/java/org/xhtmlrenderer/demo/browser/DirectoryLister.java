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
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;


/**
 * Description of the Class
 *
 * @author empty
 */
public class DirectoryLister {

    /**
     * Description of the Method
     *
     * @param file PARAM
     * @return Returns
     * @throws Exception Throws
     */
    public Document list(File file)
            throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element html = doc.createElement("html");
        Element head = doc.createElement("head");
        html.appendChild(head);
        Element style = doc.createElement("style");
        head.appendChild(style);
        StringBuffer stysb = new StringBuffer();
        stysb.append("table { background-color: #ddffdd; }");
        stysb.append(".dir { font-weight: bold; color: #ff9966; }");
        stysb.append(".file { font-weight: normal; color: #003333; }");
        style.appendChild(doc.createTextNode(stysb.toString()));
        Element body = doc.createElement("body");
        Element p = doc.createElement("p");
        body.appendChild(p);
        html.appendChild(body);
        doc.appendChild(html);

        p.appendChild(doc.createTextNode("the file " + file.toString() + " is"));
        //Uu.p("listing file: " + file);

        if (file.isDirectory()) {
            //Uu.p("is dir");
            Element table = doc.createElement("table");
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                //Uu.p("doing: " + files[i]);
                Element tr = doc.createElement("tr");
                if (files[i].isDirectory()) {
                    tr.appendChild(td(files[i].getName(), "dir", doc));
                } else {
                    tr.appendChild(td(files[i].getName(), "file", doc));
                }
                table.appendChild(tr);
            }
            body.appendChild(table);
        }

        return doc;
    }

    /**
     * Description of the Method
     *
     * @param str PARAM
     * @param cls PARAM
     * @param doc PARAM
     * @return Returns
     */
    public Element td(String str, String cls, Document doc) {
        Element td = doc.createElement("td");
        td.setAttribute("class", cls);
        td.appendChild(doc.createTextNode(str));
        return td;
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.5  2004/12/12 03:33:07  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.4  2004/12/12 02:55:10  tobega
 * Making progress
 *
 * Revision 1.3  2004/10/23 14:38:58  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */

