
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

import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.joshy.u;

public class DirectoryLister {

    public Document list(File file) throws Exception {
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
        //u.p("listing file: " + file);
        
        if(file.isDirectory()) {
            //u.p("is dir");
            Element table = doc.createElement("table");
            File[] files = file.listFiles();
            for(int i=0; i<files.length; i++) {
                //u.p("doing: " + files[i]);
                Element tr = doc.createElement("tr");
                if(files[i].isDirectory()) {
                    tr.appendChild(td(files[i].getName(),"dir",doc));
                } else {
                    tr.appendChild(td(files[i].getName(),"file",doc));
                }
                table.appendChild(tr);
            }
            body.appendChild(table);
        }
        
        return doc;
    }
    
    public Element td(String str, String cls, Document doc) {
        Element td = doc.createElement("td");
        td.setAttribute("class",cls);
        td.appendChild(doc.createTextNode(str));
        return td;
    }
}
