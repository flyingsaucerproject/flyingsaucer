/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.pdf;

import org.xhtmlrenderer.extend.FSImage;

import com.lowagie.text.Image;

public class ITextFSImage implements FSImage, Cloneable {
    private Image _image;

    public ITextFSImage(Image image) {
        _image = image;
    }

    public int getWidth() {
        return (int)_image.getPlainWidth();
    }

    public int getHeight() {
        return (int)_image.getPlainHeight();
    }

    public void scale(int width, int height) {
        if (width > 0 || height > 0) {
            int currentWith = getWidth();
            int currentHeight = getHeight();
            int targetWidth = width;
            int targetHeight = height;

            if (targetWidth == -1) {
                targetWidth = (int)(currentWith * ((double)targetHeight / currentHeight));
            }

            if (targetHeight == -1) {
                targetHeight = (int)(currentHeight * ((double)targetWidth / currentWith));
            }

            if (currentWith != targetWidth || currentHeight != targetHeight) {
                _image.scaleAbsolute(targetWidth, targetHeight);
            }
        }
    }

    public Image getImage() {
        return _image;
    }

    public Object clone() {
        return new ITextFSImage(Image.getInstance(_image));
    }
}
