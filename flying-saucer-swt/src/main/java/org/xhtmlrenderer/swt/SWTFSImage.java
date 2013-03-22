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

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.UserAgentCallback;

/**
 * 
 * @author Vianney le Clément
 * 
 */
public class SWTFSImage implements FSImage {
    private final UserAgentCallback _uac;
    private final String _uri;
    private Image _image;
    private int _width, _height;

    /**
     * Create a "null" image.
     */
    public SWTFSImage() {
        this(null, null, null);
    }

    public SWTFSImage(Image image, UserAgentCallback uac, String uri) {
        _uac = uac;
        _uri = uri;
        _image = image;
        if (image != null) {
            Rectangle bounds = image.getBounds();
            _width = bounds.width;
            _height = bounds.height;
        } else {
            _width = 10;
            _height = 10;
        }
    }

    public SWTFSImage(SWTFSImage image) {
        _uac = image._uac;
        _uri = image._uri;
        _image = image._image;
        _width = image._width;
        _height = image._height;
    }

    /**
     * Get the SWT image. Reload it from the UAC if it was disposed.
     * 
     * @return
     */
    public Image getImage() {
        if (_image != null && _image.isDisposed()) {
            SWTFSImage fsimg = (SWTFSImage) _uac.getImageResource(_uri)
                .getImage();
            if (fsimg == null) {
                _image = null;
            } else {
                _image = fsimg._image;
            }
        }
        return _image;
    }

    public int getHeight() {
        return _height;
    }

    public void setHeight(int height) {
        if (height < 0)
            return;
        _height = height;
    }

    public int getWidth() {
        return _width;
    }

    public void setWidth(int width) {
        if (width < 0)
            return;
        _width = width;
    }

    public void scale(int width, int height) {
        if (width < 0 && height < 0) {
            return;
        } else if (width < 0) {
            width = Math.round(_width
                    * (_height == 0 ? 1 : ((float) height / _height)));
        } else if (height < 0) {
            height = Math.round(_height
                    * (_width == 0 ? 1 : ((float) width / _width)));
        }
        _width = width;
        _height = height;
    }

}
