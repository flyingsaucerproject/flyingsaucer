package org.xhtmlrenderer.test;

import org.xhtmlrenderer.simple.*;
import java.awt.*;
import javax.imageio.*;
import java.io.*;
import java.awt.image.*;

public class Graphics2DRendererTest {

    public static void main(String[] args) throws Exception {
        BufferedImage img = Graphics2DRenderer.renderToImage(
            new File("demos/splash/splash.html").toURL(),
            700);
        ImageIO.write(img,"png",new File("test.png"));
    }
}
