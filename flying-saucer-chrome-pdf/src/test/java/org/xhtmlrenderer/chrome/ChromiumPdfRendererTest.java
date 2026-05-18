package org.xhtmlrenderer.chrome;

import com.codeborne.pdftest.PDF;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.xhtmlrenderer.chrome.TestUtils.printFile;

class ChromiumPdfRendererTest {
    private static final Logger log = LoggerFactory.getLogger(ChromiumPdfRendererTest.class);

    @Test
    void rendersWithCustomOptions() throws IOException {
        URL html = requireNonNull(
                getClass().getClassLoader().getResource("org/xhtmlrenderer/chrome/sample.html"));

        byte[] bytes = new ChromiumPdfRenderer()
                .setOptions(new ChromiumPdfOptions().addExtraArg("--hide-scrollbars"))
                .setTimeout(Duration.ofMinutes(2))
                .renderToPdf(html);

        PDF pdf = printFile(log, bytes, "options-chrome.pdf");
        assertThat(pdf).containsText("Hello Chromium");
    }

    @Test
    void writesToTargetPath(@TempDir Path tmp) throws IOException {
        URL html = requireNonNull(
                getClass().getClassLoader().getResource("org/xhtmlrenderer/chrome/sample.html"));
        Path outputPdf = tmp.resolve("out.pdf");

        new ChromiumPdfRenderer().renderToPdf(html, outputPdf);

        assertThat(Files.size(outputPdf)).isGreaterThan(1000);
    }

    @Test
    void failsWithMissingExplicitBinary(@TempDir Path tmp) {
        Path bogus = tmp.resolve("not-chrome");
        URL html = requireNonNull(
                getClass().getClassLoader().getResource("org/xhtmlrenderer/chrome/sample.html"));

        ChromiumPdfRenderer renderer = new ChromiumPdfRenderer().setBinaryPath(bogus);

        assertThatThrownBy(() -> renderer.renderToPdf(html))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("not found");
    }
}
