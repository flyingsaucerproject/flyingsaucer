package org.joshy.html;

import org.joshy.u;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.net.URL;
import java.net.MalformedURLException;

public class ImageUtil {
    public static Image loadImage(Context c, String src) throws MalformedURLException {
        if(src.startsWith("http")) {
            return new ImageIcon(new URL(src)).getImage();
        } else {
            //u.p("src = " + src);
            URL base = c.getBaseURL();
            if(base != null) {
                URL image_url = new URL(base,src);
                //u.p("image url = " + image_url);
                return new ImageIcon(image_url).getImage();
            }
            return new ImageIcon(src).getImage();
        }
    }
}
