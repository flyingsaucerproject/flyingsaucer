
/* 
 * {{{ header & license 
 * Copyright (c) 2004 Joshua Marinacci 
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; either version 2.1 
 * of the License, or (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. 
 * }}} 
 */

package org.xhtmlrenderer.util;



import org.joshy.u;

import org.xhtmlrenderer.layout.Context;

import javax.swing.ImageIcon;

import java.awt.Image;

import java.net.URL;

import java.net.MalformedURLException;



public class ImageUtil {

    public static Image loadImage(Context c, String src) throws MalformedURLException {

        Image img = null;

        if(src.startsWith("http")) {

            img = new ImageIcon(new URL(src)).getImage();

        } else {

            //u.p("src = " + src);

            URL base = c.getBaseURL();

            if(base != null) {

                URL image_url = new URL(base,src);

                //u.p("image url = " + image_url);

                img = new ImageIcon(image_url).getImage();

            } else {

                img = new ImageIcon(src).getImage();

            }

        }
        if(img != null && img.getWidth(null) == -1) {

            return null;

        }

        return img;

    }

}

