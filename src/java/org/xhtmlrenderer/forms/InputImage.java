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
package org.xhtmlrenderer.forms;

import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.w3c.dom.Element;
import org.xhtmlrenderer.layout.Context;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class InputImage extends InputButton {

    /** Constructor for the InputImage object */
    public InputImage() { }

    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param elem  PARAM
     * @return      Returns
     */
    public JComponent createComponent( Context c, Element elem ) {
        JButton comp = (JButton)super.createComponent( c, elem );

        if ( elem.hasAttribute( "src" ) ) {
            String src = elem.getAttribute( "src" );
            comp.setIcon( new ImageIcon( src ) );
            comp.setText( null );
            comp.setBorderPainted( false );
            comp.setMargin( new Insets( 0, 0, 0, 0 ) );
            comp.setPreferredSize( new Dimension( comp.getIcon().getIconHeight(),
                    comp.getIcon().getIconHeight() ) );

        }
        return comp;
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.3  2004/10/23 13:40:28  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

