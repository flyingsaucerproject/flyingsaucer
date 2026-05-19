package org.xhtmlrenderer.chrome;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

import static java.util.Objects.requireNonNull;

@Disabled("Long-running performance benchmark — run manually with -Dtest=Benchmark")
class Benchmark {
    private static final Logger log = LoggerFactory.getLogger(Benchmark.class);

    @Test
    void compareBackends() throws Exception {
        int iterations = Integer.getInteger("benchmark.iterations", 1000);
        URL html = requireNonNull(
                getClass().getClassLoader().getResource("org/xhtmlrenderer/chrome/benchmark.html"));

        log.info("Warming up...");
        var unusedPdf = org.xhtmlrenderer.pdf.Html2Pdf.fromUrl(html);
        var unusedChrome = Html2Pdf.fromUrl(html);

        log.info("Rendering {} PDFs with flying-saucer-pdf...", iterations);
        long pdfStart = System.nanoTime();
        long pdfBytes = 0;
        for (int i = 0; i < iterations; i++) {
            pdfBytes += org.xhtmlrenderer.pdf.Html2Pdf.fromUrl(html).length;
        }
        long pdfMillis = (System.nanoTime() - pdfStart) / 1_000_000;

        log.info("Rendering {} PDFs with flying-saucer-chrome-pdf...", iterations);
        long chromeStart = System.nanoTime();
        long chromeBytes = 0;
        for (int i = 0; i < iterations; i++) {
            chromeBytes += Html2Pdf.fromUrl(html).length;
        }
        long chromeMillis = (System.nanoTime() - chromeStart) / 1_000_000;

        log.info("=== Results (lower is better) ===");
        log.info("flying-saucer-pdf:        {} ms total | {} ms/pdf | {} bytes/pdf",
                pdfMillis, pdfMillis / (double) iterations, pdfBytes / iterations);
        log.info("flying-saucer-chrome-pdf: {} ms total | {} ms/pdf | {} bytes/pdf",
                chromeMillis, chromeMillis / (double) iterations, chromeBytes / iterations);
        log.info("Ratio (chrome / pdf):     {}x", chromeMillis / (double) pdfMillis);
    }
}
