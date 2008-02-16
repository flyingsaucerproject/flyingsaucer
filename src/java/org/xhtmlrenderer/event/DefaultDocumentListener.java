/*
 * {{{ header & license
 * Copyright (c) 2008 Patrick Wright
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
package org.xhtmlrenderer.event;

/**
 * Default, do-nothing implementation of a {@link DocumentListener}; implements all methods in {@link DocumentListener}
 * but the methods do nothing. Subclass this class and override whichever methods you need to trap.
 */
public class DefaultDocumentListener implements DocumentListener {
    /**
     * {@inheritDoc}
     */
    public void documentStarted() {
    }

    /**
     * {@inheritDoc}
     */
    public void documentLoaded() {
    }

    /**
     * {@inheritDoc}
     */
    public void onLayoutException(Throwable t) {
    }

    /**
     * {@inheritDoc}
     */
    public void onRenderException(Throwable t) {
    }
}
