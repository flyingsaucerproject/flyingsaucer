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
package org.xhtmlrenderer.render;



/**
 * Description of the Class
 *
 * @author   empty
 */
public class BlockBox extends Box {

    /** Description of the Field */
    public boolean auto_width = true;

    /** Description of the Field */
    public boolean auto_height = true;

    //public boolean inline = false;

    /** Description of the Field */
    public boolean display_block = true;

    /** Description of the Field */
    public boolean display_inline_block = false;

    /*
     * public boolean isInline() {
     * return inline;
     * }
     * public boolean isBlock() {
     * return !inline;
     * }
     */
    /** Constructor for the BlockBox object */
    public BlockBox() {

        super();

    }

    /**
     * Constructor for the BlockBox object
     *
     * @param x  PARAM
     * @param y  PARAM
     * @param w  PARAM
     * @param h  PARAM
     */
    public BlockBox( int x, int y, int w, int h ) {

        super( x, y, w, h );

    }


    /**
     * Converts to a String representation of the object.
     *
     * @return   A string representation of the object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer();

        sb.append( "BlockBox:" );

        sb.append( super.toString() );

        if ( this.fixed ) {

            sb.append( " position: fixed" );

        }

        if ( this.right_set ) {

            sb.append( " right = " + this.right );

        }

        //+ " right = " + this.right;

        // + " width = " + auto_width + " height = " + auto_height;

        return sb.toString();
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.2  2004/10/23 13:50:26  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

