/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Who?
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
package org.xhtmlrenderer.resource;

import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.swing.MutableFSImage;
import org.xhtmlrenderer.swing.AWTFSImage;
import org.xml.sax.InputSource;

/**
 * @author Administrator
 */
public class ImageResource extends AbstractResource {
    private final String _imageUri;
    private FSImage _img;

    //HACK: at least for now, till we know what we want to do here
    public ImageResource(final String uri, FSImage img) {
        super((InputSource) null);
        _imageUri = uri;
        _img = img;
    }

    public FSImage getImage() {
        return _img;
    }

    public boolean isLoaded() {
        return _img instanceof MutableFSImage ? ((MutableFSImage) _img).isLoaded() : true;
    }

    public String getImageUri() {
        return _imageUri;
    }

    public boolean hasDimensions(final int width, final int height) {
        if (isLoaded()) {
            if (_img instanceof AWTFSImage) {
                AWTFSImage awtfi = (AWTFSImage) _img;
                return awtfi.getWidth() == width && awtfi.getHeight() == height;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.6  2009/05/15 16:20:13  pdoubleya
 * ImageResource now tracks the URI for the image that was created and handles mutable images.
 *
 * Revision 1.4  2007/04/11 21:09:06  pdoubleya
 * Remove commented block
 *
 * Revision 1.3  2006/02/02 02:47:36  peterbrant
 * Support non-AWT images
 *
 * Revision 1.2  2005/06/25 17:23:34  tobega
 * first refactoring of UAC: ImageResource
 *
 * Revision 1.1  2005/02/03 20:39:35  pdoubleya
 * Added to CVS.
 *
 *
 */