/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Torbj�rn Gannholm
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
package org.xhtmlrenderer.docx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.swing.NaiveUserAgent;
import org.xhtmlrenderer.util.XRLog;

import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfReader;

public class Docx4jUserAgent extends NaiveUserAgent {

//    private static final int IMAGE_CACHE_CAPACITY = 32;
//
//    private SharedContext _sharedContext;
//
//    private final Docx4jDocxOutputDevice _outputDevice;
//
//    public Docx4jUserAgent(Docx4jDocxOutputDevice outputDevice) {
//		super(IMAGE_CACHE_CAPACITY);
//		_outputDevice = outputDevice;
//    }

    protected byte[] readStream(InputStream is) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(is.available());
        byte[] buf = new byte[10240];
        int i;
        while ( (i = is.read(buf)) != -1) {
            out.write(buf, 0, i);
        }
        out.close();
        return out.toByteArray();
    }

    public Docx4JFSImage getDocx4JImageResource(String uri) {
                
        InputStream is = resolveAndOpenStream(uri);
        if (is != null) {
            try {
                return new Docx4JFSImage(readStream(is));
            } catch (Exception e) {
                XRLog.exception("Can't read image file; unexpected problem for URI '" + uri + "'", e);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return null;
    }

//    private void scaleToOutputResolution(Image image) {
//        float factor = _sharedContext.getDotsPerPixel();
//        image.scaleAbsolute(image.getPlainWidth() * factor, image.getPlainHeight() * factor);
//    }
//
//    public SharedContext getSharedContext() {
//        return _sharedContext;
//    }
//
//    public void setSharedContext(SharedContext sharedContext) {
//        _sharedContext = sharedContext;
//    }
}
