package org.xhtmlrenderer.demo.core;

import org.xhtmlrenderer.simple.Graphics2DRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Created by infi.he on 2016/4/12.
 */
public class Graphics2DRendererTest {

    public static void main(String[] args)  throws Exception {
        BufferedImage img = Graphics2DRenderer.renderToImageAutoSize(new File("demos/fontface/fontface.html").toURI().toURL().toExternalForm(),
                700, BufferedImage.TYPE_INT_ARGB);
        ImageIO.write(img, "png", new File("test.png"));
    }
}
