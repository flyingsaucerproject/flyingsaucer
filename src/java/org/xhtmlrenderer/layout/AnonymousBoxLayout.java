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

package org.xhtmlrenderer.layout;

import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.css.*;
import org.xhtmlrenderer.util.u;
import org.w3c.dom.*;

public class AnonymousBoxLayout extends InlineLayout {
    public AnonymousBoxLayout() {
    }
    private Element parent;
    private Node text;

    // use the passed in 'text'  since that's what we are
    // really laying out instead of the 'node', which is really the
    // parent element.
    public Box createBox(Context c, Node node) {
        AnonymousBlockBox block = new AnonymousBlockBox(text,c);
        return block;
    }
    public void prepareBox(Box box, Context c) {

        box.border = new Border();
        box.padding = new Border();
        box.margin = new Border();

    }

    public Box layout(Context c, Element parent, Node text) {
        this.parent = parent;
        this.text = text;
        //Box box = new AnonymousBlockBox(text);
        Box box = super.layout(c,parent);
        //u.p("AnonymousBoxLayout.layout: returning: " + box);
        return box;
    }
    public Box layoutChildren(Context c, Box box) {
        //u.p("AnonymousBoxLayout.layoutChildren() noop" + box);
        return super.layoutChildren(c,box);
        //return box;
    }

}
