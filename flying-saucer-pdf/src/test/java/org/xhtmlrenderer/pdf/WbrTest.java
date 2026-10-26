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
 * A line breaks at {@code <wbr>}, which prints nothing, see
 * <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/763">issue 763</a>.
 */
public class WbrTest {
    private static final Logger log = LoggerFactory.getLogger(WbrTest.class);

    @Test
    void lineBreaksAtWbr() throws IOException {
        String html = """
                <html xmlns="http://www.w3.org/1999/xhtml">
                  <head><style>body { font: 16px sans-serif; }</style></head>
                  <body><div style="width: 60px">xx aaa<wbr/>bbb</div></body>
                </html>
                """;
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        renderer.createPDF(out);

        PDF pdf = printFile(log, out.toByteArray(), "wbr.pdf");
        assertThat(pdf.text.lines().map(String::strip)).containsExactly("xx aaa", "bbb");
    }
}
