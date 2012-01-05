/*
 * {{{ header & license
 * Copyright (c) 2008 Patrick Wright
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

package org.xhtmlrenderer.experimental;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author patrick
 */
public class ImageLoader {
    private JFrame frame;
    private JPanel panel;
    private WorkQueue queue;

    public static void main(String[] args) {
        new ImageLoader().run();
    }

    private void run() {
        queue = new WorkQueue(5);
        final List imageUrls = buildImageUrlList();
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                panel = new JPanel();
                frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().add(BorderLayout.CENTER, panel);
                frame.pack();
                frame.setSize(800, 600);
                frame.setVisible(true);
                startImageLoading(imageUrls);
            }
        });
    }

    private void startImageLoading(final List imageUrls) {
        for (Iterator it = imageUrls.iterator(); it.hasNext();) {
            final String url = (String) it.next();
            queue.queueTask(new ImageLoadRunnable(url));
        }
    }

    private void addImageIcon(final BufferedImage img) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                panel.add(new JLabel(new ImageIcon(img)));
                panel.validate();
            }
        });
    }

    private List buildImageUrlList() {
        return Arrays.asList(new String[]{
                "http://webcvs.freedesktop.org/tango/tango-icon-theme/32x32/actions/address-book-new.png?view=co",
                "http://webcvs.freedesktop.org/tango/tango-icon-theme/32x32/actions/appointment-new.png?view=co",
                "http://webcvs.freedesktop.org/tango/tango-icon-theme/32x32/actions/contact-new.png?view=co",
                "http://webcvs.freedesktop.org/tango/tango-icon-theme/32x32/actions/bookmark-new.png?view=co"
        });
    }

    private class ImageLoadRunnable implements Runnable {
        private final String urlString;

        public ImageLoadRunnable(String urlString) {
            this.urlString = urlString;
        }

        public void run() {
            try {
                final URL url = new URL(urlString);
                final URLConnection connection = url.openConnection();
                BufferedInputStream in = new BufferedInputStream(connection.getInputStream());

                final ImageInputStream stream = ImageIO.createImageInputStream(in);
                final Iterator readers = ImageIO.getImageReaders(stream);
                final ImageReader reader = (ImageReader) readers.next();
                IIOReadProgressListener progressListener = new IIOReadProgressListener() {
                    public void sequenceStarted(ImageReader imageReader, int i) {
                    }

                    public void sequenceComplete(ImageReader imageReader) {
                    }

                    public void imageStarted(ImageReader imageReader, int i) {
                    }

                    public void imageProgress(ImageReader imageReader, float v) {
                    }

                    public void imageComplete(ImageReader imageReader) {
                        System.out.println("   complete " + url);
                    }

                    public void thumbnailStarted(ImageReader imageReader, int i, int i1) {
                    }

                    public void thumbnailProgress(ImageReader imageReader, float v) {
                    }

                    public void thumbnailComplete(ImageReader imageReader) {
                    }

                    public void readAborted(ImageReader imageReader) {
                        System.out.println("ABORT " + url);
                    }
                };
                reader.addIIOReadProgressListener(progressListener);
                reader.setInput(stream, true);
                BufferedImage img = reader.read(0);
                addImageIcon(img);
            } catch (IOException e) {
                System.err.println("Can't load image URL " + urlString + ": " + e.getMessage());
            }
        }

        public String toString() {
            return "ImageLoad: " + urlString;
        }
    }
}
