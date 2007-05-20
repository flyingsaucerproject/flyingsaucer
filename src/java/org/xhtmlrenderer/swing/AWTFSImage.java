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
package org.xhtmlrenderer.swing;

import java.awt.Image;
import java.awt.image.BufferedImage;

import org.xhtmlrenderer.extend.FSImage;

public abstract class AWTFSImage implements FSImage {
    public static FSImage createLegacyImage(Image img) {
        return new OldAWTFSImage(img);
    }

    protected AWTFSImage() { }

    public abstract Image getImage();

    static class OldAWTFSImage extends AWTFSImage {
        private Image img;

        public OldAWTFSImage(Image img) {
            // we "clean" the image here to force conversion to a Toolkit
            // image (hence "old" AWT) instead of a BufferedImage
            if ( img instanceof BufferedImage )
                img = img.getScaledInstance(img.getWidth(null), img.getHeight(null), Image.SCALE_FAST);

            this.img = img;
        }

        public int getWidth() {
            return img.getWidth(null);
        }

        public int getHeight() {
            return img.getHeight(null);
        }

        public Image getImage() {
            return img;
        }

        public void scale(int width, int height) {
            img = img.getScaledInstance(width, height, Image.SCALE_FAST);
        }
    }
}
