/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
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
package org.xhtmlrenderer.swing;

import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.layout.LayoutContext;

import java.awt.*;

/**
 * An ImageReplacedElement is a {@link ReplacedElement} that contains a {@link java.awt.Image}. It's used as a
 * container for images included within XML being rendered. The image contained is immutable.
 */
public interface ImageReplacedElement extends ReplacedElement {
    /**
     * The image we're replacing
     */
    Image getImage();

    @Override
    default void detach(LayoutContext c) {
        // nothing to do in this case
    }

    @Override
    default boolean isRequiresInteractivePaint() {
        return true;
    }

    @Override
    default boolean hasBaseline() {
        return false;
    }

    @Override
    default int getBaseline() {
        return 0;
    }
}
