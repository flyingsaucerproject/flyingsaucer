package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.xhtmlrenderer.pdf.TestUtils.printFile;

public class ConcurrentPdfGenerationTest {
    private static final Logger log = LoggerFactory.getLogger(ConcurrentPdfGenerationTest.class);

    @Test
    public void samplePdf() throws IOException {
        byte[] bytes = generatePdf("sample.html");
        PDF pdf = printFile(log, bytes, "sample.pdf");
        verifyPdf(pdf);
    }

    @Test
    public void concurrentPdfGeneration() throws InterruptedException {
        try (ScheduledExecutorService timer = newScheduledThreadPool(1)) {
            timer.scheduleWithFixedDelay(System::gc, 0, 15, MILLISECONDS);

            List<Throwable> failures = new ArrayList<>();
            try (ExecutorService pool = newFixedThreadPool(4)) {
                for (int j = 0; j < 20; j++) {
                    final int i = j;
                    pool.submit(() -> {
                        try {
                            byte[] pdf = generatePdf("sample.html");
                            verifyPdf(new PDF(pdf));
                            log.info("Check #{} ok", i);
                        } catch (Throwable e) {
                            log.error("Check #{} failed: ", i, e);
                            failures.add(e);
                        }
                    });
                }

                pool.shutdown();
                assert pool.awaitTermination(250, SECONDS) : "Timeout!";
            }

            assertThat(failures).isEmpty();
        }
    }

    private void verifyPdf(PDF pdf) {
        assertThat(pdf).containsText("Bill To:", "John doe", "john.do@mail.com");
        assertThat(pdf).containsText("Invoice #:", "INV-666");
        assertThat(pdf).containsText("Invoice Date:", "Sep 27, 2023");
        assertThat(pdf).doesNotContainText("Password", "Secret");
    }

    private byte[] generatePdf(String htmlPath) {
        URL htmlUrl = requireNonNull(currentThread().getContextClassLoader().getResource(htmlPath),
                () -> "Test resource not found: " + htmlPath);
        return Html2Pdf.fromUrl(htmlUrl);
    }
}
