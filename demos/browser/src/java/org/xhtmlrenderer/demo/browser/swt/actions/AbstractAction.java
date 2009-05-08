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
package org.xhtmlrenderer.demo.browser.swt.actions;


public abstract class AbstractAction implements Action {

    private String _text;
    private int _style;
    private int _shortcut;
    private String _icon;

    public AbstractAction(String text, int style, int shortcut, String icon) {
        _text = text;
        _style = style;
        _shortcut = shortcut;
        _icon = icon;
    }

    public int getStyle() {
        return _style;
    }

    public String getIcon() {
        return _icon;
    }

    public int getShortcut() {
        return _shortcut;
    }

    public String getText() {
        return _text;
    }

}
