/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.swing.NaiveUserAgent;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.ContentTypeDetectingInputStreamWrapper;
import org.xhtmlrenderer.util.ImageUtil;
import org.xhtmlrenderer.util.XRLog;

import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfReader;

public class ITextUserAgent extends NaiveUserAgent {
    private static final int IMAGE_CACHE_CAPACITY = 32;

    private SharedContext _sharedContext;

    private final ITextOutputDevice _outputDevice;

    public ITextUserAgent(ITextOutputDevice outputDevice) {
        super(Configuration.valueAsInt("xr.image.cache-capacity", IMAGE_CACHE_CAPACITY));
        _outputDevice = outputDevice;
    }

    private byte[] readStream(InputStream is) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(is.available());
        byte[] buf = new byte[10240];
        int i;
        while ((i = is.read(buf)) != -1) {
            out.write(buf, 0, i);
        }
        out.close();
        return out.toByteArray();
    }

    public ImageResource getImageResource(String uriStr) {
        ImageResource resource;
        if (!ImageUtil.isEmbeddedBase64Image(uriStr)) {
            uriStr = resolveURI(uriStr);
        }
        resource = (ImageResource) _imageCache.get(uriStr);

        if (resource == null) {
            if (ImageUtil.isEmbeddedBase64Image(uriStr)) {
                resource = loadEmbeddedBase64ImageResource(uriStr);
                _imageCache.put(uriStr, resource);
            } else {
                InputStream is = resolveAndOpenStream(uriStr);
                if (is != null) {
                    try {
                        ContentTypeDetectingInputStreamWrapper cis=new ContentTypeDetectingInputStreamWrapper(is);
                        is=cis;
                        if (cis.isPdf()) {
                            URI uri = new URI(uriStr);
                            PdfReader reader = _outputDevice.getReader(uri);
                            PDFAsImage image = new PDFAsImage(uri);
                            Rectangle rect = reader.getPageSizeWithRotation(1);
                            image.setInitialWidth(rect.getWidth() * _outputDevice.getDotsPerPoint());
                            image.setInitialHeight(rect.getHeight() * _outputDevice.getDotsPerPoint());
                            resource = new ImageResource(uriStr, image);
                        } else {
                            Image image = Image.getInstance(readStream(is));
                            scaleToOutputResolution(image);
                            resource = new ImageResource(uriStr, new ITextFSImage(image));
                        }
                        _imageCache.put(uriStr, resource);
                    } catch (Exception e) {
                        XRLog.exception("Can't read image file; unexpected problem for URI '" + uriStr + "'", e);
                    } finally {
                        try {
                            is.close();
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                }
            }
        }
        if (resource != null) {
            FSImage image = resource.getImage();
            if (image instanceof ITextFSImage) {
                image = (FSImage) ((ITextFSImage) resource.getImage()).clone();
            }
            resource = new ImageResource(resource.getImageUri(), image);
        } else {
            resource = new ImageResource(uriStr, null);
        }
        return resource;
    }
    
    private ImageResource loadEmbeddedBase64ImageResource(final String uri) {
        try {
            byte[] buffer = ImageUtil.getEmbeddedBase64Image(uri);
            Image image = Image.getInstance(buffer);
            scaleToOutputResolution(image);
            return new ImageResource(null, new ITextFSImage(image));
        } catch (Exception e) {
            XRLog.exception("Can't read XHTML embedded image.", e);
        }
        return new ImageResource(null, null);
    }

    private void scaleToOutputResolution(Image image) {
        float factor = _sharedContext.getDotsPerPixel();
        if (factor != 1.0f) {
            image.scaleAbsolute(image.getPlainWidth() * factor, image.getPlainHeight() * factor);
        }
    }

    public SharedContext getSharedContext() {
        return _sharedContext;
    }

    public void setSharedContext(SharedContext sharedContext) {
        _sharedContext = sharedContext;
    }
}
