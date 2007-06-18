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

import java.awt.*;
import java.awt.image.BufferedImage;

import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.util.ImageUtil;

/**
 * An ImageReplacedElement is a {@link ReplacedElement} that contains a {@link java.awt.Image}. It's used as a
 * container for images included within XML being rendered. The image contained is immutable.
 */
public class ImageReplacedElement implements ReplacedElement {
    private final Image _image;
    
    private Point _location = new Point(0, 0);

    /**
     * Creates a new ImageReplacedElement and scales it to the size specified if either width or height has a valid
     * value (values are > -1), otherwise original size is preserved. The idea is that the image was loaded at
     * a certain size (that's the Image instance here) and that at the time we create the ImageReplacedElement
     * we have a target W/H we want to use.
     *
     * @param image An image.
     * @param targetWidth The width we'd like the image to have, in pixels.
     * @param targetHeight The height we'd like the image to have, in pixels.
     */
    public ImageReplacedElement(Image image, int targetWidth, int targetHeight) {
		if (targetWidth > 0 || targetHeight > 0) {
            int w = image.getWidth(null);
            int h = image.getHeight(null);
			int newW = targetWidth > 0 ? targetWidth : w;
			int newH = targetHeight > 0 ? targetHeight : h;
			if (w != newW || h != newH) {
                if (image instanceof BufferedImage) {
                    image = ImageUtil.getScaledInstance((BufferedImage) image, newW, newH);
                } else {
                    image = image.getScaledInstance(newW, newH, Image.SCALE_FAST);
                }
            }
        }
        _image = image;
    }

    /** {@inheritDoc} */
    public void detach(LayoutContext c) {
        // nothing to do in this case
    }

    /** {@inheritDoc} */
    public int getIntrinsicHeight() {
        return _image.getHeight(null);
    }

    /** {@inheritDoc} */
    public int getIntrinsicWidth() {
        return _image.getWidth(null);
    }

    /** {@inheritDoc} */
    public Point getLocation() {
        return _location;
    }

    /** {@inheritDoc} */
    public boolean isRequiresInteractivePaint() {
        return true;
    }

    /** {@inheritDoc} */
    public void setLocation(int x, int y) {
        _location = new Point(x, y);
    }

    /**
     * The image we're replacing.
     * @return see desc
     */
    public Image getImage() {
        return _image;
    }
}
