
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

import org.xml.sax.*;
import java.util.logging.*;
import javax.swing.JTextArea;

public class ValidationHandler implements ErrorHandler {
    public void setTextArea(JTextArea jta) {
        this.jta = jta;
    }
    public static Logger logger = Logger.getLogger("app.browser");
    protected JTextArea jta;
    protected void print(String str) {
        if(jta != null) {
            jta.append(str);
        }
    }
    
    public void error(SAXParseException ex) {
        print("error: " + print(ex));
    }
    
    public void fatalError(SAXParseException ex) {
        print("fatal error: " + print(ex));
    }
    
    public void warning(SAXParseException ex) {
        print("warning: " + print(ex));
    }
    
    public String print(SAXParseException ex) {
        StringBuffer sb = new StringBuffer();
        sb.append("Exception: " + ex.getMessage());
        sb.append("failed at column : " + ex.getColumnNumber() +
        " on line " + ex.getLineNumber());
        sb.append("entity:\n" + ex.getPublicId() + "\n" + ex.getSystemId());
        return sb.toString();
    }
        
}
