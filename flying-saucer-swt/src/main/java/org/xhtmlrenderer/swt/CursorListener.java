/*
 * {{{ header & license
 * Copyright (c) 2007 Vianney le Clément
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Cursor;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.FSDerivedValue;
import org.xhtmlrenderer.render.Box;

/**
 * Listener to change cursor according to css style.
 * 
 * @author Vianney le Clément
 * 
 */
public class CursorListener implements MouseMoveListener {
    private final BasicRenderer _parent;

    /**
     * Construct a CursorListener and add it to the parent.
     * 
     * @param parent
     */
    public CursorListener(BasicRenderer parent) {
        _parent = parent;
        parent.addMouseMoveListener(this);
    }

    public void mouseMove(MouseEvent e) {
        Box box = _parent.find(e.x, e.y);
        if (box == null) {
            return;
        }

        // TODO taken from CalculatedStyle, but shouldn't really
        FSDerivedValue value = box.getStyle().valueByName(CSSName.CURSOR);
        int cursor = SWT.CURSOR_ARROW;

        if (value == IdentValue.AUTO || value == IdentValue.DEFAULT) {
            cursor = SWT.CURSOR_ARROW;
        } else if (value == IdentValue.CROSSHAIR) {
            cursor = SWT.CURSOR_CROSS;
        } else if (value == IdentValue.POINTER) {
            cursor = SWT.CURSOR_HAND;
        } else if (value == IdentValue.MOVE) {
            cursor = SWT.CURSOR_SIZEALL;
        } else if (value == IdentValue.E_RESIZE) {
            cursor = SWT.CURSOR_SIZEE;
        } else if (value == IdentValue.NE_RESIZE) {
            cursor = SWT.CURSOR_SIZENE;
        } else if (value == IdentValue.NW_RESIZE) {
            cursor = SWT.CURSOR_SIZENW;
        } else if (value == IdentValue.N_RESIZE) {
            cursor = SWT.CURSOR_SIZEN;
        } else if (value == IdentValue.SE_RESIZE) {
            cursor = SWT.CURSOR_SIZESE;
        } else if (value == IdentValue.SW_RESIZE) {
            cursor = SWT.CURSOR_SIZESW;
        } else if (value == IdentValue.S_RESIZE) {
            cursor = SWT.CURSOR_SIZES;
        } else if (value == IdentValue.W_RESIZE) {
            cursor = SWT.CURSOR_SIZEW;
        } else if (value == IdentValue.TEXT) {
            cursor = SWT.CURSOR_IBEAM;
        } else if (value == IdentValue.WAIT) {
            cursor = SWT.CURSOR_WAIT;
        } else if (value == IdentValue.HELP) {
            cursor = SWT.CURSOR_HELP;
        } else if (value == IdentValue.PROGRESS) {
            // We don't have a cursor for this by default, maybe we need
            // a custom one for this (but I don't like it).
            cursor = SWT.CURSOR_APPSTARTING;
        }

        Cursor c = null;
        if (cursor != SWT.CURSOR_ARROW) {
            c = _parent.getDisplay().getSystemCursor(cursor);
        }
        _parent.setCursor(c);
    }

}
