package org.xhtmlrenderer.test;

import org.xhtmlrenderer.simple.Graphics2DRenderer;
import org.xhtmlrenderer.util.Uu;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;

public class AllPageTest {

    public static void main(String[] args) throws Exception {
        new AllPageTest().run();
    }

    public void run() {
        try {
            String demosDir = "d:/java/javanet/xhtmlrenderer/demos/browser/xhtml/new";
            File[] files = new File(demosDir).listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith("xhtml");
                }
            });
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                try {
                    render(file);
                } catch ( Exception ex ) {
                    ex.printStackTrace();
                }
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    private void render(File file) throws Exception {
        System.out.println("\n\n*** Rendering page " + file.getName() + " ***\n\n");
        long total = 0;
        int cnt = 1;
        String page = file.toURL().toExternalForm();
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
