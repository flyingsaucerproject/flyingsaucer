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

import java.net.URI;
import java.net.URL;

import org.xhtmlrenderer.extend.FSImage;

public class PDFAsImage implements FSImage {
    private URI _source;
    
    private float _width;
    private float _height;
    
    private float _unscaledWidth;
    private float _unscaledHeight;
    
    public PDFAsImage(URI source) {
        _source = source;
    }
    
    public int getWidth() {
        return (int)_width;
    }

    public int getHeight() {
        return (int)_height;
    }

    public void scale(int width, int height) {
        float targetWidth = width;
        float targetHeight = height;
        
        if (width == -1) {
            targetWidth = getWidthAsFloat() * (targetHeight / getHeight());
        }
        
        if (height == -1) {
            targetHeight = getHeightAsFloat() * (targetWidth / getWidth());
        }
        
        _width = targetWidth;
        _height = targetHeight;
    }
    
    public URI getURI() {
        return _source;
    }
    
    public void setInitialWidth(float width) {
        if (_width == 0) {
            _width = width;
            _unscaledWidth = width;
        }
    }
    
    public void setInitialHeight(float height) {
        if (_height == 0) {
            _height = height;
            _unscaledHeight = height;
        }
    }
    
    public float getWidthAsFloat() {
        return _width;
    }
    
    public float getHeightAsFloat() {
        return _height;
    }

    public float getUnscaledHeight() {
        return _unscaledHeight;
    }

    public void setUnscaledHeight(float unscaledHeight) {
        _unscaledHeight = unscaledHeight;
    }

    public float getUnscaledWidth() {
        return _unscaledWidth;
    }

    public void setUnscaledWidth(float unscaledWidth) {
        _unscaledWidth = unscaledWidth;
    }
    
    public float scaleHeight() {
        return _height / _unscaledHeight;
    }
    
    public float scaleWidth() {
        return _width / _unscaledWidth;
    }

}
