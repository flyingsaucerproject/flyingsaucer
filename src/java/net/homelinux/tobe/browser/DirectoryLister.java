
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

import java.io.*;
import org.w3c.dom.*;
import org.xhtmlrenderer.util.u;
import net.homelinux.tobe.renderer.XRDocument;
import net.homelinux.tobe.renderer.UserAgentCallback;

public class DirectoryLister {
    
    StringBuffer sb;

    public XRDocument list(UserAgentCallback ua, File file) throws Exception {
        sb = new StringBuffer();
        
        sb.append("<html xmlns='http://www.w3.org/1999/xhtml'>");
        sb.append("<head>");
            sb.append("<title>Directory listing of ").append(file.toString()).append("</title>");
            sb.append("<style type='text/css'>");
                sb.append("body { background-color: #ddffdd; }");
                sb.append(".dir { font-weight: bold; color: #ff9966; }");
                sb.append(".file { font-weight: normal; color: #003333; }");
            sb.append("</style>");
        sb.append("</head>");
        sb.append("<body>");
            sb.append("<h1>Directory listing of ").append(file.toString()).append("</h1>");
        
        if(file.isDirectory()) {
            //sb.append("<table>");
            File[] files = file.listFiles();
            for(int i=0; i<files.length; i++) {
                //sb.append("<tr>");
                sb.append("<p>");
                if(files[i].isDirectory()) {
                    td(files[i].getName(),"dir");
                } else {
                    td(files[i].getName(),"file");
                }
                //sb.append("</tr>");
                sb.append("</p>");
            }
            //sb.append("</table>");
        }
        sb.append("</body>");
        sb.append("</html>");
        
        XRDocument doc = new XRDocument(ua, new StringReader(sb.toString()), file.toURI());
        sb = null;
        return doc;
    }
    
    private void td(String str, String cls) {
        //sb.append("<td class='").append(cls).append("'>");
        sb.append("<span class='").append(cls).append("'>");
            sb.append(str);
        //sb.append("</td>");
        sb.append("</span>");
    }
}
