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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class AbsoluteLayoutManager implements LayoutManager {
    /** Constructor for the AbsoluteLayoutManager object */
    public AbsoluteLayoutManager() { }

    /**
     * Adds a feature to the LayoutComponent attribute of the
     * AbsoluteLayoutManager object
     *
     * @param name  The feature to be added to the LayoutComponent attribute
     * @param comp  The feature to be added to the LayoutComponent attribute
     */
    public void addLayoutComponent( String name, Component comp ) { }

    /**
     * Description of the Method
     *
     * @param target  PARAM
     */
    public void layoutContainer( Container target ) {

        int ncomponents = target.countComponents();
        for ( int i = 0; i < ncomponents; i++ ) {
            Component comp = target.getComponent( i );
            int x = comp.getX();
            int y = comp.getY();
            Dimension size = comp.getPreferredSize();
            comp.reshape( x, y, size.width, size.height );
        }

    }

    /**
     * Description of the Method
     *
     * @param parent  PARAM
     * @return        Returns
     */
    public Dimension minimumLayoutSize( Container parent ) {
        return parent.size();
    }

    /**
     * Description of the Method
     *
     * @param parent  PARAM
     * @return        Returns
     */
    public Dimension preferredLayoutSize( Container parent ) {
        return parent.size();
    }

    /**
     * Description of the Method
     *
     * @param comp  PARAM
     */
    public void removeLayoutComponent( Component comp ) { }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.2  2004/10/23 13:40:27  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

