package org.xhtmlrenderer.test;

import org.xhtmlrenderer.simple.Graphics2DRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class Graphics2DRendererTest {

    public static void main(String[] args) throws Exception {
        BufferedImage img = Graphics2DRenderer.renderToImage(new File("demos/splash/splash.html").toURL().toExternalForm(),
                700);
        ImageIO.write(img, "png", new File("test.png"));
    }
}
