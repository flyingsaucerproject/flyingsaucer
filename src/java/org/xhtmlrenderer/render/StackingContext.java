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

import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.XRRuntimeException;

import java.awt.Graphics2D;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2005-okt-16
 * Time: 14:45:58
 * To change this template use File | Settings | File Templates.
 */
public abstract class StackingContext {

    public static StackingContext newInstance() {
        String scClassName = Configuration.valueFor("xr.stackingcontext.class", "org.xhtmlrenderer.render.OldRenderingStackingContext");
        try {
            Class scClass = Class.forName(scClassName);
            StackingContext sc = (StackingContext) scClass.newInstance();
            return sc;
        } catch (ClassNotFoundException e) {
            throw new XRRuntimeException(e.getLocalizedMessage(), e);
        } catch (IllegalAccessException e) {
            throw new XRRuntimeException(e.getLocalizedMessage(), e);
        } catch (InstantiationException e) {
            throw new XRRuntimeException(e.getLocalizedMessage(), e);
        }

    }

    protected StackingContext() {

    }

    abstract public void addBlock(Renderable b);

    abstract public void addLine(Renderable b);

    //HACK: Context should not be used here
    abstract public void render(RenderingContext c, Graphics2D g2, double top, double bottom);

    abstract public void addAbsolute(Renderable absolute);

    abstract public void addFloat(Renderable floater);
}
