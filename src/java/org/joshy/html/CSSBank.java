package org.joshy.html;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.*;
import java.awt.Color;
import org.joshy.u;
import org.joshy.x;
import org.joshy.html.css.*;

import com.steadystate.css.*;
import com.steadystate.css.parser.*;
import org.w3c.dom.*;
import org.w3c.dom.css.*;
import org.w3c.css.sac.*;

public class CSSBank extends CSSAccessor {
    /* internal vars */
    
    public List sheets;
    public List style_nodes;
    public List styles;
    CSSParser parser;
    

    public CSSBank() {
        sheets = new ArrayList();
        style_nodes = new ArrayList();
        styles = new ArrayList();
        parser = new CSSParser(this);
    }
    
    
    public void parse(Reader reader) throws IOException {
        parser.parse(reader);
    }
    public void parse(String reader) throws IOException {
        parser.parse(reader);
    }
    
    public void parseInlineStyles(Element elem) throws IOException {
        parser.parseInlineStyles(elem);
    }
    private void pullOutStyles(CSSStyleSheet sheet) throws IOException {
        parser.pullOutStyles(sheet);
    }

    
    /* ========= property accessors ============ */
    private Object getProperty(Node node, String prop) {
        if(node.getNodeType() == node.TEXT_NODE) {
            return getProperty(node.getParentNode(),prop);
        }
        if(node.getNodeType() == node.ELEMENT_NODE) {
            return getProperty((Element)node,prop);
        }
        u.p("unknown node type: " + node);
        u.p("type = " + node.getNodeType());
        return null;
    }
    
    public CSSValue getProperty(Element elem, String prop, boolean inherit) {
        //u.p("looking at: " + elem.getNodeName() + " prop = " + prop + " inherit " + inherit);
        RuleFinder rf = new RuleFinder(this.styles);
        CSSStyleDeclaration style_dec = rf.findRule(elem,prop,inherit);
        //u.p("got style: " + style_dec);
        if(style_dec == null) {
            //u.p("print there is no style declaration at all for: " + elem.getNodeName());
            //u.p("looking for property: " + prop);
            return null;
        }
        CSSValue val = style_dec.getPropertyCSSValue(prop);
        if(val == null) {
            //u.p("elem " + elem.getNodeName() + " doesn't have the property: " + prop);
            if(elem.getParentNode() != null && inherit) {
                //u.p("going up: " + elem.getNodeName() + " -> " + elem.getParentNode().getNodeName() + " prop = " + prop);
                if(elem.getParentNode() instanceof Element) {
                    return getProperty((Element)elem.getParentNode(),prop,inherit);
                }
            }
            return null;
        }
        //u.p("returning: " + val);
        return val;
    }
    
    
    

}
