
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

import java.util.HashMap;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xhtmlrenderer.util.u;
import org.xhtmlrenderer.forms.InputButton;
import org.xhtmlrenderer.forms.*;
import org.xhtmlrenderer.table.*;

/**
Returns the appropriate layout for a given node. Currently this hard codes
specific elements to specific layouts (for example, h1 is always an inline layout).
Instead this should use the display attribute to figure it out.  The Inline and
Box layouts work together so that you don't need to specify one or the other,
always just use Inline. For lists, tables, and comments, though, we still need
to specify the particular layout here.

*/
public class LayoutFactory {
    private static HashMap element_map = new HashMap();
    public static Layout getLayout(Node elem) {
        //u.p("node = " + elem);
        //u.p("getting layout for: " + elem.getNodeName());
        if(elem.getNodeType() == elem.ELEMENT_NODE) {
            if(elem.getNodeName().equals("div")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("p")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("span")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("u")) { return new InlineLayout(); }
            /* deprectated in xhtml strict
            if(elem.getNodeName().equals("strike")) { return new InlineLayout(); }
            */
            if(elem.getNodeName().equals("pre")) { return new InlineLayout(); }

            /* these are not deprecated in xhtml strict but aren't used much.
               here just for completeness */
            if(elem.getNodeName().equals("tt")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("b")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("i")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("big")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("small")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("em")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("strong")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("dfn")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("code")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("samp")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("kbd")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("var")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("cite")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("ins")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("del")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("sup")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("sub")) { return new InlineLayout(); }
            
            if(elem.getNodeName().equals("a")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("h1")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("h2")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("h3")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("h4")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("h5")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("h6")) { return new InlineLayout(); }
            
            // list stuff
            if(elem.getNodeName().equals("ol")) { return new ListLayout(); }
            if(elem.getNodeName().equals("ul")) { return new ListLayout(); }
            if(elem.getNodeName().equals("li")) { return new InlineLayout(); }
            
            // image stuff
            if(elem.getNodeName().equals("img")) { return new ImageLayout(); }
            
            // table stuff
            if(elem.getNodeName().equals("table")) { return new TableLayout2(); }
            if(elem.getNodeName().equals("td")) { return new TableCellLayout(); }
            if(elem.getNodeName().equals("th")) { return new TableCellLayout(); }
            
            if(elem.getNodeName().equals("br")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("font")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("hr")) { return new NullLayout(); }
            if(elem.getNodeName().equals("form")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("select")) { return new InputSelect(); }
            if(elem.getNodeName().equals("option")) { return new NullLayout(); }
                //u.p("here: " + elem.getNodeName());
            if(elem.getNodeName().equals("textarea")) { 
                return new InputTextArea(); 
            }
            if(elem.getNodeName().equals("input")) {
                Element el = (Element)elem;
                String type = el.getAttribute("type");
                if(type == null) { return new InputButton(); }
                if(type.equals("button")) { return new InputButton(); }
                if(type.equals("checkbox")) { return new InputCheckbox(); }
                //if(type.equals("file")) { return new InputCheckbox(); }
                if(type.equals("hidden")) { return new NullLayout(); }
                if(type.equals("image")) { return new InputImage(); }
                if(type.equals("password")) { return new InputPassword(); }
                if(type.equals("radio")) { return new InputRadio(); }
                if(type.equals("reset")) { return new InputButton(); }
                if(type.equals("submit")) { return new InputButton(); }
                if(type.equals("text")) { return new InputText(); }
            }
            if(elem.getNodeName().equals("body")) { return new InlineLayout(); }
            if(elem.getNodeName().equals("head")) { return new InlineLayout(); }
        }

        // skip whitespace only nodes
        if(elem.getNodeType() == elem.TEXT_NODE) {
            if(elem.getNodeValue().trim().equals("")) {
                //u.p("skipping an empty text node");
                return new NullLayout();
            }
        }

        if(elem.getNodeType() == elem.TEXT_NODE) {
            //return new SpanLayout();
            return new AnonymousBoxLayout();
        }


        if(elem.getNodeType() == elem.COMMENT_NODE) {
            return new NullLayout();
        }
        
        Layout lyt = getCustomLayout(elem);
        if(lyt != null) {
            return lyt;
        }
        u.p("error! returning null! type = " + elem.getNodeType());
        u.p("name = " + elem.getNodeName());
        
        /*
        u.p("node = " + elem);
        u.p("attribute = " + elem.ATTRIBUTE_NODE);
        u.p("cdata = " + elem.CDATA_SECTION_NODE);
        u.p("coomment = " + elem.COMMENT_NODE);
        u.p("frag = " + elem.DOCUMENT_FRAGMENT_NODE);
        u.p("document = " + elem.DOCUMENT_NODE);
        u.p("doctype = " + elem.DOCUMENT_TYPE_NODE);
        u.p("element = " + elem.ELEMENT_NODE);
        u.p("entity = " + elem.ENTITY_NODE);
        u.p("entity ref = " + elem.ENTITY_REFERENCE_NODE);
        u.p("notation = " + elem.NOTATION_NODE);
        u.p("processing inst = " + elem.PROCESSING_INSTRUCTION_NODE);
        u.p("text node = " + elem.TEXT_NODE);
        */
        return new InlineLayout();
    }
    
    
    public static void addCustomLayout(String elem_name, Layout layout) {
        element_map.put(elem_name,layout);
    }
    
    private static Layout getCustomLayout(Node elem) {
        if(element_map.containsKey(elem.getNodeName())) {
            return (Layout) element_map.get(elem.getNodeName());
        }
        return null;
    }
    
    
    
    
    
    
    
    
    
    
    
}

