package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class ConcurrentPdfGenerationTest {
    private static final Logger log = LoggerFactory.getLogger(ConcurrentPdfGenerationTest.class);

    @Test
    public void samplePdf() {
        byte[] pdf = generatePdf("sample.html");
        verifyPdf(pdf);
    }

    @Test
    public void concurrentPdfGeneration() throws InterruptedException {
        ScheduledExecutorService timer = newScheduledThreadPool(1);
        timer.scheduleWithFixedDelay(System::gc, 0, 15, MILLISECONDS);

        List<Throwable> failures = new ArrayList<>();
        ExecutorService pool = newFixedThreadPool(10);
        for (int j = 0; j < 200; j++) {
            final int i = j;
            pool.submit(() -> {
                try {
                    byte[] pdf = generatePdf("sample.html");
                    verifyPdf(pdf);
                    log.info("Check #{} ok", i);
                }
                catch (Throwable e) {
                    log.error("Check #{} failed: ", i, e);
                    failures.add(e);
                }
            });
        }

        pool.shutdown();
        assert pool.awaitTermination(250, SECONDS) : "Timeout!";
        
        assertThat(failures).isEmpty();
    }

    private void verifyPdf(byte[] pdfBytes) {
        PDF pdf = new PDF(pdfBytes);
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
