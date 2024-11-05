package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;

public class WordBreakTest {
    private static final Logger log = LoggerFactory.getLogger(WordBreakTest.class);

    @Test
    void breakAll() throws IOException {

        byte[] pdf = Html2Pdf.fromClasspathResource("org/xhtmlrenderer/pdf/break-all.html");
        printFile(pdf, "break-all.pdf");
        assertThat(new PDF(pdf)).containsExactText("HelloWorld1\nHelloWorld2\nHelloWorld3\nHelloWorld4\nHelloWorld5\n");
    }

    private static void printFile(byte[] pdf, String filename) throws IOException {
        File file = new File("target", filename);
        try (FileOutputStream o = new FileOutputStream(file)) {
            o.write(pdf);
        }
        log.info("Generated PDF: {}", file.getAbsolutePath());
    }
}
