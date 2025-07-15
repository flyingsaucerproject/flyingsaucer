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

import com.lowagie.text.Image;
import org.jspecify.annotations.NonNull;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.Size;

public class ITextFSImage implements FSImage, Cloneable {
    private final Image image;

    public ITextFSImage(Image image) {
        this.image = image;
    }

    @Override
    public int getWidth() {
        return (int) image.getPlainWidth();
    }

    @Override
    public int getHeight() {
        return (int) image.getPlainHeight();
    }

    @NonNull
    @Override
    public FSImage scale(int width, int height) {
        Size current = new Size(getWidth(), getHeight());
        Size target = current.scale(width, height);

        if (!target.equals(current)) {
            Image scaledImage = Image.getInstance(image);
            scaledImage.scaleAbsolute(target.width(), target.height());
            return new ITextFSImage(scaledImage);
        }
        return this;
    }

    public Image getImage() {
        return image;
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public Object clone() {
        return new ITextFSImage(Image.getInstance(image));
    }
}
