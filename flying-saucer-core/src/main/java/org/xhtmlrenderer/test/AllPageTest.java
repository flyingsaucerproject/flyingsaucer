package org.xhtmlrenderer.test;

import org.xhtmlrenderer.simple.Graphics2DRenderer;
import org.xhtmlrenderer.util.Uu;

import java.io.File;
import java.util.Date;

public class AllPageTest {
    public static void main(String[] args) throws Exception {
        new AllPageTest().run();
    }

    public void run() throws Exception {
        String demosDir = "d:/java/javanet/xhtmlrenderer/demos/browser/xhtml/new";
        File[] files = new File(demosDir).listFiles((dir, name) -> name.endsWith("xhtml"));
        for (File file : files) {
            render(file);
        }
    }

    private void render(File file) throws Exception {
        System.out.println("\n\n*** Rendering page " + file.getName() + " ***\n\n");
        long total = 0;
        int cnt = 1;
        String page = file.toURI().toURL().toExternalForm();
        System.out.println("Testing with page " + page);
        for (int i = 0; i < cnt; i++) {
            Date start = new Date();
            Graphics2DRenderer.renderToImage(page, 700, 700);
            Date end = new Date();
            long diff = (end.getTime() - start.getTime());
            Uu.p("ms = " + diff);
            if (i > 4) total += diff;
        }
        long avg = total / cnt;
        System.out.println("average : " + avg);
    }
}
