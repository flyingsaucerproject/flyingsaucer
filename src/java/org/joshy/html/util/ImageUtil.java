package org.joshy.html.util;



import org.joshy.u;

import org.joshy.html.Context;

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

