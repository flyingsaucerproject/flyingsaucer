
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

package org.joshy.html.css;

import java.awt.*;
import org.joshy.html.Context;
import java.util.HashMap;
import org.joshy.u;

public class FontResolverTest extends FontResolver {
    String[] available_fonts;
    HashMap instance_hash;
    HashMap available_fonts_hash;
    public FontResolverTest() {
        GraphicsEnvironment gfx = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] available_fonts = gfx.getAvailableFontFamilyNames();
        //u.p("available fonts =");
        //u.p(available_fonts);
        instance_hash = new HashMap();
        
        // preload the font map with the font names as keys
        // don't add the actual font objects because that would be a waste of memory
        // we will only add them once we need to use them
        // put empty strings in instead
        available_fonts_hash = new HashMap();
        for(int i=0; i<available_fonts.length; i++) {
            available_fonts_hash.put(available_fonts[i],new String());
        }
        
        // preload sans, serif, and monospace into the available font hash
        available_fonts_hash.put("Serif",new Font("Serif",Font.PLAIN,1));
        available_fonts_hash.put("SansSerif",new Font("SansSerif",Font.PLAIN,1));
        //u.p("put in sans serif");
        available_fonts_hash.put("Monospaced",new Font("Monospaced",Font.PLAIN,1));
    }
    
    protected Font createFont(Font root_font, float size, String weight, String style) {
        int font_const = Font.PLAIN;
        if(weight != null && weight.equals("bold")) {
            font_const = font_const | Font.BOLD;
        }
        if(style != null && style.equals("italic")) {
            font_const = font_const | Font.ITALIC;
        }
        
        Font fnt = root_font.deriveFont(font_const,size);
        return fnt;
    }
    
    protected String getFontInstanceHashName(String name, float size, String weight, String style) {
        return name + "-" + size + "-" + weight + "-" + style;
    }
    
    protected Font resolveFont(Context c, String font, float size, String weight, String style) {
        // strip off the "s if they are there
        if(font.startsWith("\"")) {
            font = font.substring(1);
        }
        if(font.endsWith("\"")) {
            font = font.substring(0,font.length()-1);
        }
        
        
        //u.p("final font = " + font);
        // normalize the font name
        if(font.equals("serif")) {
            font = "Serif";
        }
        if(font.equals("sans-serif")) {
            font = "SansSerif";
        }
        if(font.equals("monospace")) {
            font = "Monospaced";
        }
        

        // assemble a font instance hash name
        String font_instance_name = getFontInstanceHashName(font,size,weight,style);
        //u.p("looking for font: " + font_instance_name);
        // check if the font instance exists in the hash table
        if(instance_hash.containsKey(font_instance_name)) {
            // if so then return it
            return (Font) instance_hash.get(font_instance_name);
        }
        
        
        //u.p("font lookup failed for: " + font_instance_name);
        //u.p("searching for : " + font + " " + size + " " + weight + " " + style);
        
        
        // if not then 
        //  does the font exist
        if(available_fonts_hash.containsKey(font)) {
            //u.p("found an available font for: " + font);
            Object value = available_fonts_hash.get(font);
            // have we actually allocated the root font object yet?
            Font root_font = null;
            if(value instanceof Font) {
                root_font = (Font)value;
            } else {
                root_font = new Font(font,Font.PLAIN,1);
                available_fonts_hash.put(font,root_font);
            }
            
            // now that we have a root font, we need to create the correct version of it
            Font fnt = createFont(root_font,size,weight,style);
            
            // add the font to the hash so we don't have to do this again
            instance_hash.put(font_instance_name,fnt);
            return fnt;
        }
        
        // we didn't find any possible matching font, so just return null
        return null;
    }
    
    public Font resolveFont(Context c, String[] families, float size, String weight, String style) {
        //u.p("familes = ");
        //u.p(families);
        // for each font family
        if(families != null) {
            for(int i=0; i<families.length; i++) {
                Font font = resolveFont(c,families[i],size,weight,style);
                if(font != null) { return font; }
            }
        }
        
        // if we get here then no font worked, so just return default sans
        //u.p("pulling out: -" + available_fonts_hash.get("SansSerif") + "-");
        try {
            Font fnt = createFont((Font)available_fonts_hash.get("SansSerif"),size,weight,style);
            instance_hash.put(getFontInstanceHashName("SansSerif",size,weight,style),fnt);
            //u.p("subbing in base sans : " + fnt);
            return fnt;
        } catch (Exception ex) {
            org.joshy.u.p("exception: " + ex);
            return c.getGraphics().getFont();
        }

    }
}

