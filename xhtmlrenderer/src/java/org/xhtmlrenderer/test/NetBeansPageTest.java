package org.xhtmlrenderer.test;

import org.xhtmlrenderer.simple.Graphics2DRenderer;
import org.xhtmlrenderer.util.Uu;

import java.io.File;
import java.util.Date;

public class NetBeansPageTest {

    public static void main(String[] args) throws Exception {
        long total = 0;
        int cnt = 1;
        String demosDir = "d:/data/projects/xhtmlrenderer/demos";
        String page = demosDir + "/browser/xhtml/layout/multicol/glish/one.html";
        //String page = demosDir + "/browser/xhtml/hamlet.xhtml";
        //String page = demosDir + "/splash/splash.html";
        System.out.println("Testing with page " + page);
        for (int i = 0; i < cnt; i++) {
            Date start = new Date();
            Graphics2DRenderer.renderToImage(
                    new File(page).toURL().toExternalForm(),
                    700, 700);
            Date end = new Date();
            long diff = (end.getTime() - start.getTime());
            Uu.p("ms = " + diff);
            if (i > 4) total += diff;
        }
        long avg = total / cnt;
        System.out.println("average : " + avg);
    }
}
