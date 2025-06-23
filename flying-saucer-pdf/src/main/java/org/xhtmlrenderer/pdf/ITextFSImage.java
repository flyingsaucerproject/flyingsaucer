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

import org.jspecify.annotations.NonNull;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.Size;

public class ITextFSImage implements FSImage {
    protected final byte[] image;
    protected final Size size;
    protected final String uri;

    public ITextFSImage(byte[] image, Size size, String uri) {
        this.image = image;
        this.size = size;
        this.uri = uri;
    }

    @Override
    public int getWidth() {
        return size.width();
    }

    @Override
    public int getHeight() {
        return size.height();
    }

    @NonNull
    @Override
    public FSImage scale(int width, int height) {
        return new ITextFSImage(image, size.scale(width, height), uri);
    }

    public byte[] getImage() {
        return image;
    }
}
