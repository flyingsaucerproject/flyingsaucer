
/*
 *  {{{ header & license
 *  Copyright (c) 2004 Joshua Marinacci
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *  }}}
 */
package org.xhtmlrenderer.layout;

import java.util.HashMap;
import java.util.logging.Level;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.forms.*;
import org.xhtmlrenderer.forms.InputButton;
import org.xhtmlrenderer.table.*;

import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.render.Renderer;

/**
 *  Returns the appropriate layout for a given node. Currently this hard codes
 *  specific elements to specific layouts (for example, h1 is always an inline
 *  layout). Instead this should use the display attribute to figure it out. The
 *  Inline and Box layouts work together so that you don't need to specify one
 *  or the other, always just use Inline. For lists, tables, and comments,
 *  though, we still need to specify the particular layout here.
 *
 * @author     jmarinacci
 * @created    October 18, 2004
 */

public class LayoutFactory {
    private static HashMap element_map = new HashMap();
    static {
        initializeLayouts();
    }


    /**
     *  <p>
     *
     *  Associate a particular layout with a certain element name. This lets you
     *  add new layouts to the system or override existing ones. The following
     *  example overrides the default layout for paragraphs with the custom
     *  <code>FunnyLayout</code>.
     *  <pre>Layout lt = new FunnyLayout();
     *LayoutFactory.addCustomLayout("p",lt);
     *</pre>
     *
     * @param  elem_name  The string name of the element to be added
     * @param  layout     The layout to associate with the specified element
     */
    public static void addCustomLayout(String elem_name, Layout layout) {
        element_map.put(elem_name, layout);
    }


    /**
     *  <p>
     *
     *  Retreive the correct Layout for the specified Node</p>
     *
     * @param  elem  Element to retrieve layout for.
     * @return       the correct layout for this node
     * @see          org.xhtmlrenderer.layout.Layout
     */
    public static Layout getLayout(Node elem) {
        // pull from the hasthable first
        Layout lyt = getCustomLayout(elem);
        if (lyt != null) {
            return lyt;
        }

        // we have to do the inputs manually since they don't depend on
        // the element name
        if (elem.getNodeType() == elem.ELEMENT_NODE) {
            if (elem.getNodeName().equals("input")) {
                Element el = (Element) elem;
                String type = el.getAttribute("type");
                if (type == null) {
                    return new InputButton();
                }
                if (type.equals("button")) {
                    return new InputButton();
                }
                if (type.equals("checkbox")) {
                    return new InputCheckbox();
                }
                //if(type.equals("file")) { return new InputCheckbox(); }
                if (type.equals("hidden")) {
                    return new NullLayout();
                }
                if (type.equals("image")) {
                    return new InputImage();
                }
                if (type.equals("password")) {
                    return new InputPassword();
                }
                if (type.equals("radio")) {
                    return new InputRadio();
                }
                if (type.equals("reset")) {
                    return new InputButton();
                }
                if (type.equals("submit")) {
                    return new InputButton();
                }
                if (type.equals("text")) {
                    return new InputText();
                }
            }
        }

        // skip whitespace only nodes
        if (elem.getNodeType() == elem.TEXT_NODE) {
            if (elem.getNodeValue().trim().equals("")) {
                return new NullLayout();
            }
        }

        if (elem.getNodeType() == elem.TEXT_NODE) {
            return new AnonymousBoxLayout();
        }

        if (elem.getNodeType() == elem.COMMENT_NODE) {
            return new NullLayout();
        }

        XRLog.layout(Level.WARNING, "error! returning null! type = " + elem.getNodeType());
        XRLog.layout(Level.INFO, "error! returning null! type = " + elem.getNodeType());
        XRLog.layout(Level.INFO, "name = " + elem.getNodeName());

        XRLog.layout(Level.INFO, "node = " + elem);
        XRLog.layout(Level.INFO, "attribute = " + elem.ATTRIBUTE_NODE);
        XRLog.layout(Level.INFO, "cdata = " + elem.CDATA_SECTION_NODE);
        XRLog.layout(Level.INFO, "coomment = " + elem.COMMENT_NODE);
        XRLog.layout(Level.INFO, "frag = " + elem.DOCUMENT_FRAGMENT_NODE);
        XRLog.layout(Level.INFO, "document = " + elem.DOCUMENT_NODE);
        XRLog.layout(Level.INFO, "doctype = " + elem.DOCUMENT_TYPE_NODE);
        XRLog.layout(Level.INFO, "element = " + elem.ELEMENT_NODE);
        XRLog.layout(Level.INFO, "entity = " + elem.ENTITY_NODE);
        XRLog.layout(Level.INFO, "entity ref = " + elem.ENTITY_REFERENCE_NODE);
        XRLog.layout(Level.INFO, "notation = " + elem.NOTATION_NODE);
        XRLog.layout(Level.INFO, "processing inst = " + elem.PROCESSING_INSTRUCTION_NODE);
        XRLog.layout(Level.INFO, "text node = " + elem.TEXT_NODE);
        return new InlineLayout();
    }
    
    public Renderer getRenderer(Node node) {
        return getLayout(node);
    }


    /**
     *  Initialize the standard layouts. Called by a static initializer.
     */
    private static void initializeLayouts() {
        System.out.println("initalizing layouts");
        InlineLayout inline = new InlineLayout();
        addCustomLayout("div", inline);
        addCustomLayout("p", inline);
        addCustomLayout("span", inline);
        addCustomLayout("u", inline);
        addCustomLayout("pre", inline);
        addCustomLayout("b", inline);
        addCustomLayout("i", inline);
        addCustomLayout("big", inline);
        addCustomLayout("small", inline);
        addCustomLayout("em", inline);
        addCustomLayout("strong", inline);
        addCustomLayout("dfn", inline);
        addCustomLayout("code", inline);
        addCustomLayout("samp", inline);
        addCustomLayout("kbd", inline);
        addCustomLayout("var", inline);
        addCustomLayout("cite", inline);
        addCustomLayout("ins", inline);
        addCustomLayout("del", inline);
        addCustomLayout("sup", inline);
        addCustomLayout("sub", inline);
        addCustomLayout("a", inline);
        addCustomLayout("h1", inline);
        addCustomLayout("h2", inline);
        addCustomLayout("h3", inline);
        addCustomLayout("h4", inline);
        addCustomLayout("h5", inline);
        addCustomLayout("h6", inline);

        addCustomLayout("ol", new ListLayout());
        addCustomLayout("ul", new ListLayout());
        addCustomLayout("li", inline);
        addCustomLayout("img", new ImageLayout());
        addCustomLayout("table", new TableLayout2());
        addCustomLayout("td", new TableCellLayout());
        addCustomLayout("th", new TableCellLayout());

        addCustomLayout("body", inline);
        addCustomLayout("head", inline);

        addCustomLayout("br", inline);
        //addCustomLayout("font",inline);
        addCustomLayout("hr", new NullLayout());
        addCustomLayout("form", inline);
        addCustomLayout("select", new InputSelect());
        addCustomLayout("option", new NullLayout());
        addCustomLayout("textarea", new InputTextArea());

    }
    



    /**
     *  Gets the customLayout attribute of the LayoutFactory class
     *
     * @param  elem  Description of the Parameter
     * @return       The customLayout value
     */
    private static Layout getCustomLayout(Node elem) {
        if (element_map.containsKey(elem.getNodeName())) {
            return (Layout) element_map.get(elem.getNodeName());
        }
        return null;
    }

}

