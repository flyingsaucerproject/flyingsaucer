package org.xhtmlrenderer.pdf;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.pdfbox.Loader.loadPDF;
import static org.assertj.core.api.Assertions.assertThat;
import static org.xhtmlrenderer.pdf.TestUtils.pageContent;
import static org.xhtmlrenderer.pdf.TestUtils.printFile;

class BarcodeTest {
    private static final Logger log = LoggerFactory.getLogger(BarcodeTest.class);

    @ParameterizedTest
    @CsvSource({
        "code128, PR12345678",
        "code128_ucc, (01)00123456789012",
        "code39, PR12345678",
        "code39-extended, pr12345678",
        "codabar, A123456A",
        "ean13, 4006381333931",
        "ean8, 96385074",
        "upca, 036000291452",
        "upce, 04252614",
        "inter25, 12345670",
        "postnet, 12345",
        "planet, 12345678901",
        "pdf417, PR12345678",
        "datamatrix, PR12345678",
    })
    void generatesBarcodeImage(String type, String code) throws IOException {
        byte[] pdf = renderHtml("barcode-%s.pdf".formatted(type), "<img data-barcode=\"%s\" src=\"%s\"/>".formatted(type, code));

        assertThat(images(pdf)).hasSize(1);
    }

    @Test
    void barcodeType_isCaseInsensitive() throws IOException {
        byte[] pdf = renderHtml("barcode-type-uppercase.pdf", "<img data-barcode=\"CODE128\" src=\"PR12345678\"/>");

        assertThat(images(pdf)).hasSize(1);
    }

    @Test
    void typeAttribute_isSupportedForBackwardCompatibility() throws IOException {
        byte[] pdf = renderHtml("barcode-legacy-type-attribute.pdf", "<img type=\"code128\" src=\"PR12345678\"/>");

        PDImageXObject image = images(pdf).get(0);
        assertThat(image.getWidth()).isEqualTo(112);
        assertThat(image.getHeight()).isEqualTo(24);
    }

    @Test
    void dataBarcodeAttribute_takesPrecedenceOverType() throws IOException {
        byte[] pdf = renderHtml("barcode-data-barcode-over-type.pdf", "<img data-barcode=\"datamatrix\" type=\"code128\" src=\"PR12345678\"/>");

        PDImageXObject image = images(pdf).get(0);
        assertThat(image.getWidth()).isEqualTo(image.getHeight());
    }

    @Test
    void linearBarcode_isOneCssPixelPerModule() throws IOException {
        byte[] pdf = renderHtml("barcode-default-size.pdf", "<img data-barcode=\"code128\" src=\"PR12345678\"/>");

        PDImageXObject image = images(pdf).get(0);
        assertThat(image.getBitsPerComponent()).isEqualTo(1);
        assertThat(image.getWidth()).isEqualTo(112);
        assertThat(image.getHeight()).isEqualTo(24);
        // 1 CSS px = 0.75pt
        assertThat(pageContent(pdf)).contains("84 0 0 18 ");
    }

    @Test
    void linearBarcode_cssHeightMakesBarsTaller_butKeepsWidth() throws IOException {
        byte[] pdf = renderHtml("barcode-css-height.pdf", "<img data-barcode=\"code128\" src=\"PR12345678\" style=\"height: 100px\"/>");

        assertThat(pageContent(pdf)).contains("84 0 0 75 ");
    }

    @Test
    void linearBarcode_cssWidthAndHeight() throws IOException {
        byte[] pdf = renderHtml("barcode-css-width-and-height.pdf", "<img data-barcode=\"code128\" src=\"PR12345678\" style=\"width: 300px; height: 100px\"/>");

        assertThat(pageContent(pdf)).contains("225 0 0 75 ");
    }

    @Test
    void twoDimensionalBarcode_keepsAspectRatio() throws IOException {
        byte[] pdf = renderHtml("barcode-2d-css-width.pdf", "<img data-barcode=\"datamatrix\" src=\"PR12345678\" style=\"width: 100px\"/>");

        PDImageXObject image = images(pdf).get(0);
        assertThat(image.getWidth()).isEqualTo(image.getHeight());
        assertThat(pageContent(pdf)).contains("75 0 0 75 ");
    }

    @Test
    void invalidCode_rendersNoImage() throws IOException {
        byte[] pdf = renderHtml("barcode-invalid-code.pdf", "<img data-barcode=\"ean13\" src=\"not-a-number\"/>");

        assertThat(images(pdf)).isEmpty();
    }

    @Test
    void imageWithoutBarcodeType_isLoadedAsUsual() throws IOException {
        byte[] pdf = renderHtml("barcode-regular-image.pdf", "<img type=\"image/png\" src=\"classpath:/flyingsaucer.png\"/>");

        assertThat(images(pdf)).isNotEmpty();
    }

    private static byte[] renderHtml(String fileName, String body) throws IOException {
        ITextRenderer renderer = ITextRenderer.fromString("<html><head><title>barcode</title></head><body>" + body + "</body></html>");
        renderer.layout();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        renderer.createPDF(out);
        byte[] pdf = out.toByteArray();
        printFile(log, pdf, fileName);
        return pdf;
    }

    private static List<PDImageXObject> images(byte[] pdf) throws IOException {
        try (PDDocument document = loadPDF(pdf)) {
            PDResources resources = document.getPage(0).getResources();
            List<PDImageXObject> result = new ArrayList<>();
            for (COSName name : resources.getXObjectNames()) {
                if (resources.getXObject(name) instanceof PDImageXObject image) {
                    result.add(image);
                }
            }
            return result;
        }
    }
}
