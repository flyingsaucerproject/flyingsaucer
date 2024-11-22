package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestUtils {
    public static PDF printFile(Logger log, byte[] pdf, String filename) throws IOException {
        File file = new File("target", filename);
        try (FileOutputStream o = new FileOutputStream(file)) {
            o.write(pdf);
        }
        log.info("Generated PDF: {}", file.getAbsolutePath());
        return new PDF(pdf);
    }
}
