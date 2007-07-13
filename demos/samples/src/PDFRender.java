import java.io.IOException;
import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;

import org.xhtmlrenderer.pdf.ITextRenderer;
import com.lowagie.text.DocumentException;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Apr 6, 2006
 * Time: 11:13:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class PDFRender {
    public static void main(String[] args) throws IOException, DocumentException {
        if (args.length != 2) {
            System.err.println("Usage: ... [url] [pdf]");
            System.exit(1);
        }
        String url = args[0];
        if (url.indexOf("://") == -1) {
            // maybe it's a file
            File f = new File(url);
            if (f.exists()) {
                url = f.toURI().toURL().toString();
            }
        }
        createPDF(url, args[1]);
    }

    public static void createPDF(String url, String pdf)
            throws IOException, DocumentException {
        OutputStream os = null;
        try {
            os = new FileOutputStream(pdf);

            ITextRenderer renderer = new ITextRenderer();

            renderer.setDocument(url);
            renderer.layout();
            renderer.createPDF(os);

            os.close();
            os = null;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}
