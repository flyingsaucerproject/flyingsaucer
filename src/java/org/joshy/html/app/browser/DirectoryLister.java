package org.joshy.html.app.browser;

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
