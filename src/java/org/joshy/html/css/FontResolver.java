
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

import java.awt.Font;
import org.joshy.html.Context;
import org.joshy.u;

public class FontResolver {

    public Font resolveFont(Context c, String[] families, float size, String weight, String style) {
      return this.resolveFont(c.getGraphics().getFont(), families, size, weight, style);
    }
    
    public Font resolveFont(Font baseFont, String[] families, float size, String weight, String style) {
        u.on();
        u.p("resolving font from families: " + families);
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
