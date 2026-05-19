package org.xhtmlrenderer.chrome;

import com.codeborne.pdftest.PDF;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

class TestUtils {
    static PDF printFile(Logger log, byte[] pdf, String filename) throws IOException {
        File target = new File("target");
        if (!target.exists() && !target.mkdirs()) {
            throw new IOException("Failed to create directory: " + target);
        }
        File file = new File(target, filename);
        try (FileOutputStream o = new FileOutputStream(file)) {
            o.write(pdf);
        }
        log.info("Generated PDF: {}", file.getAbsolutePath());
        return new PDF(pdf);
    }
}
