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

import javax.swing.*;
import java.awt.*;


/**
 * An FSImage containing a java.awt.Image which can be replaced at runtime by calling setImage(). When setImage() is
 * called, the RepaintListener passed to this class in its constructor will have repaintRequested() invoked on the
 * Swing event dispatch thread. The method isLoaded() will return true once the image load has completed. Before the
 * image has loaded, a 1x1 transparent pixel will be returned from getImage().
 */
public class MutableFSImage extends AWTFSImage {
    private volatile Image img;
    private final RepaintListener repaintListener;
    private volatile boolean loaded;

    public MutableFSImage(RepaintListener repaintListener) {
        this.repaintListener = repaintListener;
        img = ImageUtil.createTransparentImage(1, 1);
    }

    public synchronized Image getImage() {
        return img;
    }

    public synchronized int getWidth() {
        return img.getWidth(null);
    }

    public synchronized int getHeight() {
        return img.getHeight(null);
    }

    public synchronized void scale(int width, int height) {
        img.getScaledInstance(width, height, Image.SCALE_DEFAULT);
    }

    public synchronized void setImage(String uri, Image newImg) {
        img = newImg;
        loaded = true;
        XRLog.general("Mutable image " + uri + " loaded, repaint requested");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                repaintListener.repaintRequested(true);
            }
        });
    }

    public boolean isLoaded() {
        return loaded;
    }
}
