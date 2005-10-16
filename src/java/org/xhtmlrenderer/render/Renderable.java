/*
 * {{{ header & license
 * Copyright (c) 2005 Torbjšrn Gannholm
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

import org.xhtmlrenderer.layout.Context;

import java.awt.Graphics2D;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2005-okt-16
 * Time: 14:51:39
 * To change this template use File | Settings | File Templates.
 */
public interface Renderable extends VerticalExtent {

    int getIndex();

    //HACK: Context should not be necessary
    void render(Context c, Graphics2D g2);
}
