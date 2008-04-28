import org.xhtmlrenderer.simple.Graphics2DRenderer;
import org.xhtmlrenderer.swing.Java2DRenderer;
import org.xhtmlrenderer.util.FSImageWriter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author patrick
 */
public class ImageRenderAlternatives {
    public static void main(String[] args) throws Exception {
        g2dRender();
        //j2dRender();
    }

    private static void g2dRender() throws Exception {
        BufferedImage bi = Graphics2DRenderer.renderToImage("http://www.w3.org/", 1024, 768, BufferedImage.TYPE_INT_RGB);
        //FSImageWriter imageWriter = new FSImageWriter();
        //imageWriter.write(bi, "/Users/patrick/Projects/Dev/Javanet/xhtmlrenderer-head/temp/w3c-img-test-g2d.jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        com.sun.image.codec.jpeg.JPEGImageEncoder jpeg = com.sun.image.codec.jpeg.JPEGCodec.createJPEGEncoder(baos);
        jpeg.encode(bi);

        // Now save to db or file
        FileOutputStream fos = new FileOutputStream("/Users/patrick/Projects/Dev/Javanet/xhtmlrenderer-head/temp/w3c-img-test-g2d-2.jpg");
        fos.write(baos.toByteArray(), 0, baos.toByteArray().length);
        fos.flush();
        fos.close();
    }

    private static void j2dRender() throws IOException {
        Java2DRenderer rend = new Java2DRenderer("http://www.w3c.org", 1024, 768);
        BufferedImage bi = rend.getImage();
        FSImageWriter imageWriter = new FSImageWriter();
        imageWriter.write(bi, "/Users/patrick/Projects/Dev/Javanet/xhtmlrenderer-head/temp/w3c-img-test.jpg");
    }
}
