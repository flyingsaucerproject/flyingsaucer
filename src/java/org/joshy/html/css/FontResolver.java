package org.joshy.html.css;

import java.awt.Font;
import org.joshy.html.Context;

public class FontResolver {

    public Font resolveFont(Context c, String[] families, float size, String weight, String style) {
      return this.resolveFont(c.getGraphics().getFont(), families, size, weight, style);
    }
    
    public Font resolveFont(Font baseFont, String[] families, float size, String weight, String style) {
        Font f = baseFont;
        
        f = f.deriveFont((float)size);

        if(weight.equals("bold")) {
            f = f.deriveFont(Font.BOLD);
        }
   
        String family = families[0];
        
        String fontname = "SansSerif";
        //u.p("family: " + family);
        if(family.equals("serif")) {
            fontname = "Serif";
        }
        if(family.equals("sans-serif")) {
            fontname = "SansSerif";
        }
        if(family.equals("monospace")) {
            fontname = "Monospaced";
        }

        
        f = new Font(fontname,f.getStyle(),f.getSize());
                
        if(style != null) {
            if(style.equals("italic")) {
                f = f.deriveFont(Font.ITALIC|f.getStyle());//.deriveFont(Font.BOLD);
                //c.getGraphics().setFont(c.getGraphics().getFont().deriveFont(Font.ITALIC));
                
            }
        }
        return f;
    }
    
}
