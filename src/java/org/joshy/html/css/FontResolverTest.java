package org.joshy.html.css;

import java.awt.*;
import org.joshy.html.Context;
import org.joshy.u;
import java.util.HashMap;

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
        available_fonts_hash.put("Serif",new Font("SansSerif",Font.PLAIN,1));
        available_fonts_hash.put("Serif",new Font("Monospaced",Font.PLAIN,1));
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
        
        //u.p("font lookup failed");
        //u.p("searching for : " + font + " " + size + " " + weight + " " + style);
        
        // if not then 
        //  does the font exist
        if(available_fonts_hash.containsKey(font)) {
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
        
        // for each font family
        for(int i=0; i<families.length; i++) {
            Font font = resolveFont(c,families[i],size,weight,style);
            if(font != null) { return font; }
        }
        
        // if we get here then no font worked, so just return default sans
        Font fnt = createFont((Font)available_fonts_hash.get("SansSerif"),size,weight,style);
        instance_hash.put(getFontInstanceHashName("SansSerif",size,weight,style),fnt);
        //u.p("subbing in base sans : " + fnt);
        return fnt;

    }
}

