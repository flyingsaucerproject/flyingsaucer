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
import org.xhtmlrenderer.util.ImageUtil;

public class AWTFSImage implements FSImage {
    private BufferedImage _image;
    
    public AWTFSImage(BufferedImage image) {
        _image = image;
    }
    
    public BufferedImage getImage() {
        return _image;
    }
    
    public int getHeight() {
        return _image.getHeight(null);
    }
    
    public int getWidth() {
        return _image.getWidth(null);
    }
    
    public void scale(int width, int height) {
        _image = ImageUtil.getScaledInstance(_image, width, height);
    }
}
