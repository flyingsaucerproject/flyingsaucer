package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDResources;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.StreamSupport;

public class TestUtils {
    public static PDF printFile(Logger log, byte[] pdf, String filename) throws IOException {
        File file = new File("target", filename);
        try (FileOutputStream o = new FileOutputStream(file)) {
            o.write(pdf);
        }
        log.info("Generated PDF: {}", file.getAbsolutePath());
        return new PDF(pdf);
    }

    public static List<String> getFontNames(PDResources res) {
        return StreamSupport.stream(res.getFontNames().spliterator(), false)
                .map(font -> getFontName(res, font))
                .toList();
    }

    private static String getFontName(PDResources res, COSName font) {
        try {
            return res.getFont(font).getFontDescriptor().getFontName();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read font name from %s".formatted(res.getCOSObject()), e);
        }
    }
}
