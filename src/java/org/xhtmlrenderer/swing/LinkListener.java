/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 xhtmlrenderer.dev.java.net
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
package org.xhtmlrenderer.swing;

import java.awt.*;
import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;
import org.w3c.dom.Element;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.util.Uu;


/**
 * Description of the Class
 *
 * @author   Who?
 */
public class LinkListener extends MouseInputAdapter {

    /** Description of the Field  */
    protected BasicPanel panel;

    /** Description of the Field */
    private Box prev;

    /**
     * Constructor for the ClickMouseListener object
     *
     * @param panel  PARAM
     */
    public LinkListener( BasicPanel panel ) {
        this.panel = panel;
    }

    /**
     * Description of the Method
     *
     * @param evt  PARAM
     */
    public void mouseEntered( MouseEvent evt ) {
        Box box = panel.findBox( evt.getX(), evt.getY() );
        setCursor( box );
    }

    /**
     * Description of the Method
     *
     * @param evt  PARAM
     */
    public void mouseExited( MouseEvent evt ) {
        Box box = panel.findBox( evt.getX(), evt.getY() );
        setCursor( box );
    }

    /**
     * Description of the Method
     *
     * @param evt  PARAM
     */
    public void mousePressed( MouseEvent evt ) { }

    /**
     * Description of the Method
     *
     * @param evt  PARAM
     */
    public void mouseReleased( MouseEvent evt ) {
        Box box = panel.findBox( evt.getX(), evt.getY() );
        if ( box == null ) {
            return;
        }

        Element elem = box.element;
        if ( elem == null ) {
            return;
        }

        if ( panel.getContext().getNamespaceHandler().getLinkUri( elem ) != null ) {
            linkClicked( box, evt );
        }
    }

    /**
     * Description of the Method
     *
     * @param evt  PARAM
     */
    public void mouseMoved( MouseEvent evt ) {
        Box box = panel.findBox( evt.getX(), evt.getY() );
        setCursor( box );
    }

    /**
     * Description of the Method
     *
     * @param evt  PARAM
     */
    public void mouseDragged( MouseEvent evt ) {
        Box box = panel.findBox( evt.getX(), evt.getY() );
        setCursor( box );
    }

    /**
     * Description of the Method
     *
     * @param box  PARAM
     * @param evt  PARAM
     */
    public void linkClicked( Box box, MouseEvent evt ) {
        panel.repaint();
        try {
            Element elem = box.element;
            if ( elem.hasAttribute( "href" ) ) {
                panel.setDocumentRelative( elem.getAttribute( "href" ) );
            }
        } catch ( Exception ex ) {
            Uu.p( ex );
        }
    }

    /**
     * Sets the cursor attribute of the LinkListener object
     *
     * @param box  The new cursor value
     */
    private void setCursor( Box box ) {
        if ( prev == box || box == null ) {
            return;
        }

        if ( box.element == null ) {
            return;
        }

        if ( panel.getContext().getNamespaceHandler().getLinkUri( box.element ) != null ) {
            if ( !panel.getCursor().equals( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) ) ) {
                panel.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
            }
        } else {
            if ( !panel.getCursor().equals( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) ) ) {
                panel.setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );
            }
        }

        prev = box;
    }
}

