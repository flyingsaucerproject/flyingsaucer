
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

package org.xhtmlrenderer.layout;

import java.awt.Font;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xhtmlrenderer.util.u;

public class TextUtil {
public static String transformText(Context c, Node node, String text) {
    Element el = null;
    if(node instanceof Element) {
        el = (Element)node;
    } else {
        el = (Element)node.getParentNode();
    }
    String text_transform = c.css.getStringProperty(el,"text-transform");
    if(text_transform != null) {
        if(text_transform.equals("lowercase")) {
            return text.toLowerCase();
        }
        if(text_transform.equals("uppercase")) {
            return text.toUpperCase();
        }
        if(text_transform.equals("capitalize")) {
            return capitalizeWords(text);
        }
    }
    return text;
}

public static String capitalizeWords(String text) {
    //u.p("start = -"+text+"-");
    if(text.length() == 0) {
        return text;
    }
    
    StringBuffer sb = new StringBuffer();
    //u.p("text = -" + text + "-");
    
    // do first letter
    //u.p("first = " + text.substring(0,1));
    /*
    if(!text.substring(0,1).equals(" ")) {
        sb.append(text.substring(0,1).toUpperCase());
    }
    */
    boolean cap = true;
    for(int i=0; i<text.length(); i++) {
        String ch = text.substring(i,i+1);
        //u.p("ch = " + ch + " cap = " + cap);
        
        if(cap) {
            sb.append(ch.toUpperCase());
        } else {
            sb.append(ch);
        }
        cap = false;
        if(ch.equals(" ")) {
            cap = true;
        }
    }
    
    //u.p("final = -"+sb.toString()+"-");
    if(sb.toString().length() != text.length()) {
        u.p("error! to strings arent the same length = -"+sb.toString()+"-"+text+"-");
    }
    return sb.toString();
}
   
public static void stripWhitespace(Context c, Node node, Element containing_block) {
    
    String white_space = c.css.getStringProperty(containing_block,"white-space");
    // if doing preformatted whitespace
    if(white_space!=null && white_space.equals("pre")) {
        return;
    }

    
    if(node == null) { return; }
    if(node.getNodeType() != node.TEXT_NODE) { return; }
    String text = node.getNodeValue();
    //text = text.trim();
    
    /*
    if(text.indexOf("\n") > 0) {
        u.p("before text = " + text);
        StringBuffer sb = new StringBuffer();
        int m = 0;
        int n = 0;
        while((n = text.indexOf("\n",m)) > 0) {
            String span = text.substring(m,n);
            u.p("span = " + span);
            sb.append(span);
            // add a space to replace the \n
            sb.append(" ");
            m = n+1;
        }
        sb.append(text.substring(m));
        text = sb.toString();
        u.p("after text = " + text);
    }
    */
    // spaces at the start of the string -> nothing
    text = text.replaceAll("^\\s+","");
    // spaces at the start of lines -> ""
    //text = text.replaceAll("\n(\\s*)","");
    // all \n -> a single space
    text = text.replaceAll("\n"," ");
    // all extra spaces -> single space
    text = text.replaceAll("\\s+"," ");
    
    // add one space to the end
    //text = text+" ";
    //u.p(text);
    
    node.setNodeValue(text);
}

}
