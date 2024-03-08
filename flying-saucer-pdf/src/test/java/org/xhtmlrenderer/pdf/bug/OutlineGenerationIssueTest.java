package org.xhtmlrenderer.pdf.bug;

import org.junit.jupiter.api.Test;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.FileOutputStream;

public class OutlineGenerationIssueTest {

    private static final String html1 = """
            <!DOCTYPE html>
            <html>
            <head><style> h2 { page-break-before: always;}</style></head><body>
            <h1>Decision #1</h1><p>Decision 1 ...</p>
            <h2>Attachment A</h2><p>Attachment A#1 ...</p>
            <h2>Attachment B</h2><p>Attachment B#1 ...</p>
            </body>
            </html>
            """;

    private static final String html2 = """
            <!DOCTYPE html>
            <html>
            <head><style> h2 { page-break-before: always;}</style></head><body>
            <h1>Decision #2</h1><p>Decision 2 ...</p>
            <h2>Attachment B</h2><p>Attachment B#2...</p>
            </body>
            </html>
            """;

    @Test
    public void outline() throws Exception {
        writePDF("target/test-outline1.pdf", new ITextOutputDevice(26.666666F), html2, html1);
        writePDF("target/test-outline2.pdf", new ITextOutputDevice(26.666666F), html1, html2);
    }

    private void writePDF(String name, ITextOutputDevice outputDevice, String html1, String html2) throws Exception {
        try (FileOutputStream stream = new FileOutputStream(name)) {
            ITextRenderer renderer = new ITextRenderer(outputDevice.getDotsPerPoint(), 20, outputDevice);

            renderer.setDocumentFromString(html1);
            renderer.layout();
            renderer.createPDF(stream, false);

            renderer.setDocumentFromString(html2);
            renderer.layout();
            renderer.writeNextDocument();

            renderer.finishPDF();
        }
    }
}
