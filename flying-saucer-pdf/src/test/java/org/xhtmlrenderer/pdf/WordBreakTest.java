package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static org.xhtmlrenderer.pdf.TestUtils.printFile;

public class WordBreakTest {
    private static final Logger log = LoggerFactory.getLogger(WordBreakTest.class);

    @Test
    void breakAll() throws IOException {
        byte[] result = Html2Pdf.fromClasspathResource("org/xhtmlrenderer/pdf/break-all.html");
        PDF pdf = printFile(log, result, "break-all.pdf");
        assertThat(pdf).containsExactText("HelloWorld1\nHelloWorld2\nHelloWorld3\nHelloWorld4\nHelloWorld5\n");
    }
}
