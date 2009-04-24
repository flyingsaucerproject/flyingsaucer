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

import org.xhtmlrenderer.util.ImageUtil;
import org.xhtmlrenderer.util.XRLog;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


/**
 * A background thread (daemon, low priority) which reads BackgroundImageLoaderItem from a BackgroundImageQueue
 * and loads the images into memory. Once images have loaded, the item's MutableFSImage will receive the newly loaded
 * image via setImage(newImage). Images, once loaded, are always BufferedImages and will always be compatible with
 * the current screen's graphics configuration. If an image cannot be loaded (network failure), a 1 x 1 pixel image
 * will be returned instead and the problem will be logged. 
 */
class ImageLoadWorker extends Thread {
    private final ImageLoadQueue queue;

    public ImageLoadWorker(ImageLoadQueue queue) {
        this.queue = queue;
        setDaemon(true);
        setPriority(Thread.MIN_PRIORITY);
    }

    public void run() {
        try {
            while (true) {
                ImageLoadItem loadItem = queue.getTask();
                BufferedImage bi = loadImage(loadItem.uri);
                loadItem.mfsImage.setImage(loadItem.uri, bi);
            }
        } catch (InterruptedException e) {
            //
        }
    }

    // attempts to open a stream from the given URI and read it into a BufferedImage. will return a 1x1 transparent
    // image if there is any problem reading the image.
    private BufferedImage loadImage(String uri) {
        InputStream is = openStreamAtUrl(uri);
        if (is != null) {
            try {
                BufferedImage img = ImageIO.read(is);
                if (img == null) {
                    throw new IOException("ImageIO.read() returned null");
                }
                return ImageUtil.makeCompatible(img);
            } catch (FileNotFoundException e) {
                XRLog.exception("Can't read image file; image at URI '" + uri + "' not found");
                return ImageUtil.createTransparentImage(1, 1);
            } catch (IOException e) {
                XRLog.exception("Can't read image file; unexpected problem for URI '" + uri + "'", e);
                return ImageUtil.createTransparentImage(1, 1);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        } else {
            return ImageUtil.createTransparentImage(1, 1);
        }

    }

    // attempts to open a connection, and a stream, to the URI provided. timeouts will be set for opening the connection
    // and reading from it. will return the stream, or null if unable to open or read or a timeout occurred.
    private InputStream openStreamAtUrl(String uri) {
        java.io.InputStream is = null;
        try {
            final URLConnection uc = new URL(uri).openConnection();

            // If using Java 5+ you can set timeouts for the URL connection--useful if the remote
            // server is down etc.; the default timeout is pretty long
            //
            //uc.setConnectTimeout(10 * 1000);
            //uc.setReadTimeout(30 * 1000);
            //
            // TODO:CLEAN-JDK1.4
            // Since we target 1.4, we use a couple of system properties--note these are only supported
            // in the Sun JDK implementation--see the Net properties guide in the JDK
            // e.g. file:///usr/java/j2sdk1.4.2_17/docs/guide/net/properties.html
            System.setProperty("sun.net.client.defaultConnectTimeout", String.valueOf(10 * 1000));
            System.setProperty("sun.net.client.defaultReadTimeout", String.valueOf(30 * 1000));

            uc.connect();

            is = uc.getInputStream();
        } catch (java.net.MalformedURLException e) {
            XRLog.exception("bad URL given: " + uri, e);
        } catch (java.io.FileNotFoundException e) {
            XRLog.exception("item at URI " + uri + " not found");
        } catch (java.io.IOException e) {
            XRLog.exception("IO problem for " + uri, e);
        }

        return is;

    }
}
