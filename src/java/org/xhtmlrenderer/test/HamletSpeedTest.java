package org.xhtmlrenderer.test;

import org.xhtmlrenderer.simple.*;
import org.xhtmlrenderer.util.*;
import java.awt.*;
import javax.imageio.*;
import java.io.*;
import java.awt.image.*;
import java.util.Date;

public class HamletSpeedTest {

    public static void main(String[] args) throws Exception {
        long total = 0;
        for(int i=0; i<10; i++) {
            Date start = new Date();
            BufferedImage img = Graphics2DRenderer.renderToImage(
                new File("demos/browser/xhtml/hamlet.xhtml").toURL(),
                700,700);
            Date end = new Date();
            long diff = (end.getTime() - start.getTime());
            u.p("ms = " + diff);
            total += diff;
        }
        long avg = total/10;
        u.p("average : " + avg);
    }
}
