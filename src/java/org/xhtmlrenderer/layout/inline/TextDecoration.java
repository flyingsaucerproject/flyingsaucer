package org.xhtmlrenderer.layout.inline;

import org.xhtmlrenderer.layout.*;
import org.w3c.dom.*;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.css.style.*;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.u;
import org.xhtmlrenderer.util.x;

public class TextDecoration {

    public static void setupTextDecoration( Context c, Node node, InlineBox box ) {
        Element el = null;
        if ( node instanceof Element ) {
            el = (Element)node;
        } else {
            el = (Element)node.getParentNode();
        }
        
        // set to defaults
        box.underline = false;
        box.strikethrough = false;
        box.overline = false;
        
        // override based on settings
        String text_decoration = c.css.getStringProperty( el, "text-decoration" );
        if ( text_decoration != null && text_decoration.equals( "underline" ) ) {
            box.underline = true;
        }
        if ( text_decoration != null && text_decoration.equals( "line-through" ) ) {
            box.strikethrough = true;
        }
        if ( text_decoration != null && text_decoration.equals( "overline" ) ) {
            box.overline = true;
        }
    }
    
    public static void setupTextDecoration( CalculatedStyle style, Node node, InlineBox box ) {
        Element el = null;
        if ( node instanceof Element ) {
            el = (Element)node;
        } else {
            el = (Element)node.getParentNode();
        }
        if(style.hasProperty("text-decoration")) {
            DerivedProperty text_decoration = style.propertyByName("text-decoration");
            DerivedValue dv = text_decoration.computedValue();
            String td = dv.asString();
            if ( td != null && td.equals( "underline" ) ) {
                box.underline = true;
            }
            if ( td != null && td.equals( "line-through" ) ) {
                box.strikethrough = true;
            }
            if ( td != null && td.equals( "overline" ) ) {
                box.overline = true;
            }
        }
    }
    
    
    public static boolean isDecoratable(Context c, Node node) {
        if ( !LayoutUtil.isReplaced(c, node ) ) {
            if ( !LayoutUtil.isFloatedBlock( node, c ) ) {
                return true;
            }
        }
        return false;
    }
    
}
