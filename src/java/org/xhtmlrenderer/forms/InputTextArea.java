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

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.w3c.dom.Element;
import org.xhtmlrenderer.layout.Context;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class InputTextArea extends FormItemLayout {

    /** Constructor for the InputTextArea object */
    public InputTextArea() { }

    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param elem  PARAM
     * @return      Returns
     */
    public JComponent createComponent( Context c, Element elem ) {
        //u.p("created a TextArea");
        int rows = 4;
        int cols = 10;
        if ( elem.hasAttribute( "rows" ) ) {
            rows = Integer.parseInt( elem.getAttribute( "rows" ) );
        }
        if ( elem.hasAttribute( "cols" ) ) {
            cols = Integer.parseInt( elem.getAttribute( "cols" ) );
        }

        JTextArea comp = new JTextArea( rows, cols );
        commonPrep( comp, elem );
        JScrollPane sp = new JScrollPane( comp );
        sp.setVerticalScrollBarPolicy( sp.VERTICAL_SCROLLBAR_ALWAYS );
        sp.setHorizontalScrollBarPolicy( sp.HORIZONTAL_SCROLLBAR_ALWAYS );
        if ( elem.getFirstChild() != null ) {
            //u.p("setting text to: " + elem.getFirstChild().getNodeValue());
            comp.setText( elem.getFirstChild().getNodeValue() );
        }

        return sp;
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.3  2004/10/23 13:40:29  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

