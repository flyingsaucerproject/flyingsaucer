package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.xhtmlrenderer.pdf.TestUtils.printFile;

/**
 * A line breaks after a zero width space, which is not printed, see
 * <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/753">issue 753</a>.
 */
public class ZeroWidthSpaceTest {
    private static final Logger log = LoggerFactory.getLogger(ZeroWidthSpaceTest.class);

    @Test
    void lineBreaksAfterZeroWidthSpace() throws IOException {
        String html = """
                <html xmlns="http://www.w3.org/1999/xhtml">
                  <head><style>body { font: 16px sans-serif; }</style></head>
                  <body><div style="width: 60px">xx aaa&#8203;bbb</div></body>
                </html>
                """;
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        renderer.createPDF(out);

        PDF pdf = printFile(log, out.toByteArray(), "zero-width-space.pdf");
        assertThat(pdf.text.lines().map(String::strip)).containsExactly("xx aaa", "bbb");
    }
}
