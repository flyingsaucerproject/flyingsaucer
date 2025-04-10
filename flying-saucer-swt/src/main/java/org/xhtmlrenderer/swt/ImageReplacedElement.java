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

import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.layout.LayoutContext;

import java.awt.*;

/**
 * ReplacedElement for {@code <img>} tags.
 *
 * @author Vianney le Clément
 *
 */
public class ImageReplacedElement implements ReplacedElement {
    private final SWTFSImage _image;
    private final Point _location = new Point(0, 0);

    public ImageReplacedElement(SWTFSImage img, int targetWidth,
            int targetHeight) {
        _image = img;
        if (targetWidth >= 0) {
            _image.setWidth(targetWidth);
        }
        if (targetHeight >= 0) {
            _image.setHeight(targetHeight);
        }
    }

    public SWTFSImage getImage() {
        return _image;
    }

    @Override
    public void detach(LayoutContext c) {
        // nothing to do
    }

    @Override
    public int getIntrinsicHeight() {
        return _image.getHeight();
    }

    @Override
    public int getIntrinsicWidth() {
        return _image.getWidth();
    }

    @Override
    public Point getLocation() {
        return _location;
    }

    @Override
    public void setLocation(int x, int y) {
        _location.setLocation(x, y);
    }

    @Override
    public boolean isRequiresInteractivePaint() {
        return true;
    }

    @Override
    public int getBaseline() {
        return 0;
    }

    @Override
    public boolean hasBaseline() {
        return false;
    }
}
