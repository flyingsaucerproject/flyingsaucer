/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Torbjörn Gannholm
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashMap;

import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.swing.NaiveUserAgent;
import org.xhtmlrenderer.util.XRLog;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Image;

public class ITextUserAgent extends NaiveUserAgent {
    private static final int IMAGE_CACHE_CAPACITY = 32;
    
    private SharedContext _sharedContext;
    
    private LinkedHashMap _imageCache =
            new LinkedHashMap(IMAGE_CACHE_CAPACITY, 0.75f, true) {
                static final long serialVersionUID = 1L;
                protected boolean removeEldestEntry(java.util.Map.Entry eldest) {
                    return size() > IMAGE_CACHE_CAPACITY;
                }
            };
            
    public ImageResource getImageResource(String uri) {
        ImageResource resource = null;
        uri = resolveURI(uri);
        resource = (ImageResource) _imageCache.get(uri);
        if (resource == null) {
            InputStream is = getInputStream(uri);
            if (is != null) {
                try {
                    Image image = Image.getInstance(new URL(uri));
                    scaleToOutputResolution(image);
                    resource = new ImageResource(new ITextFSImage(image));
                    _imageCache.put(uri, resource);
                } catch (IOException e) {
                    XRLog.exception("Can't read image file; unexpected problem for URI '" + uri + "'", e);
                } catch (BadElementException e) {
                    XRLog.exception("Can't read image file; unexpected problem for URI '" + uri + "'", e);
                }
            }
        }
        if (resource == null) {
            resource = new ImageResource(null);
        }
        return resource;
    }
    
    private void scaleToOutputResolution(Image image) {
        float factor = _sharedContext.getDotsPerPixel();
        image.scaleAbsolute(image.plainWidth() * factor, image.plainHeight() * factor);
    }

    public SharedContext getSharedContext() {
        return _sharedContext;
    }

    public void setSharedContext(SharedContext sharedContext) {
        _sharedContext = sharedContext;
    }
}
