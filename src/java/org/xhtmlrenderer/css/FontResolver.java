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
package org.xhtmlrenderer.css;

import java.awt.Font;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.util.u;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class FontResolver {

    /**
     * Description of the Method
     *
     * @param c         PARAM
     * @param families  PARAM
     * @param size      PARAM
     * @param weight    PARAM
     * @param style     PARAM
     * @return          Returns
     */
    public Font resolveFont( Context c, String[] families, float size, String weight, String style, String variant ) {
        u.p("resolving");
        return this.resolveFont( c.getGraphics().getFont(), families, size, weight, style, variant );
    }

    /**
     * Description of the Method
     *
     * @param baseFont  PARAM
     * @param families  PARAM
     * @param size      PARAM
     * @param weight    PARAM
     * @param style     PARAM
     * @return          Returns
     */
    public Font resolveFont( Font baseFont, String[] families, float size, String weight, String style, String variant ) {
        u.p("resolve font");
        //u.on();
        //u.p( "resolving font from families: " + families );
        Font f = baseFont;

        f = f.deriveFont( (float)size );

        if ( weight.equals( "bold" ) ) {
            f = f.deriveFont( Font.BOLD );
        }

        String family = families[0];

        String fontname = "SansSerif";
        //u.p("family: " + family);
        if ( family.equals( "serif" ) ) {
            fontname = "Serif";
        }
        if ( family.equals( "sans-serif" ) ) {
            fontname = "SansSerif";
        }
        if ( family.equals( "monospace" ) ) {
            fontname = "Monospaced";
        }

        f = new Font( fontname, f.getStyle(), f.getSize() );
        
        if ( style != null ) {
            if ( style.equals( "italic" ) ) {
                f = f.deriveFont( Font.ITALIC | f.getStyle() );
            }
        }

        if ( variant != null) {
            if( variant.equals("small-caps")) {
                u.p("returnning small caps");
                f = f.deriveFont((float)( ((float) f.getSize())*0.8));
            }
        }
        return f;
    }
    
    public void setFontMapping(String name, Font font) {
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.5  2004/11/12 02:23:56  joshy
 * added new APIs for rendering context, xhtmlpanel, and graphics2drenderer.
 * initial support for font mapping additions
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/11/08 21:18:20  joshy
 * preliminary small-caps implementation
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/10/23 13:03:45  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */

