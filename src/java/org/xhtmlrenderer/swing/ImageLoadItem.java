/*
 * {{{ header & license
 * Copyright (c) 2009 Patrick Wright
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
package org.xhtmlrenderer.swing;



/**
 * Denotes a URI pointing to an image that needs to be loaded. BackgroundImageLoaderItem are queued inside a
 * BackgroundImageLoader and loaded by a BackgroundImageLoaderThread.
 */
class ImageLoadItem {
    final String uri;
    final MutableFSImage mfsImage;

    public ImageLoadItem(String uri, MutableFSImage fsi) {
        this.uri = uri;
        this.mfsImage = fsi;
    }
}
