package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import com.lowagie.text.DocumentException;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xhtmlrenderer.resource.FSEntityResolver;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ConcurrentPdfGenerationTest extends TestCase {
    private static final Logger log = LoggerFactory.getLogger(ConcurrentPdfGenerationTest.class);
    
    public void testSamplePdf() {
        byte[] pdf = generatePdf("sample.html");
        verifyPdf(pdf);
    }

    public void testConcurrentPdfGeneration() throws InterruptedException {
        ScheduledExecutorService timer = newScheduledThreadPool(1);
        timer.scheduleWithFixedDelay(System::gc, 0, 15, MILLISECONDS);

        ExecutorService pool = newFixedThreadPool(20);
        for (int j = 0; j < 2000; j++) {
            final int i = j;
            pool.submit(() -> {
                try {
                    byte[] pdf = generatePdf("sample.html");
                    verifyPdf(pdf);
                    log.info("Check #{} ok", i);
                }
                catch (Throwable e) {
                    log.error("Check #{} failed: ", i, e);
                }
            });
        }

        pool.shutdown();
        assert pool.awaitTermination(250, SECONDS) : "Timeout!";
    }

    private void verifyPdf(byte[] pdfBytes) {
        PDF pdf = new PDF(pdfBytes);
        assertThat(pdf).containsText("Bill To:", "John doe", "john.do@mail.com");
        assertThat(pdf).containsText("Invoice #:", "INV-666");
        assertThat(pdf).containsText("Invoice Date:", "Sep 27, 2023");
    }

    private byte[] generatePdf(String htmlPath) {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.getSharedContext().setInteractive(false);
        renderer.getSharedContext().getTextRenderer().setSmoothingThreshold(0);

        URL htmlUrl = requireNonNull(Thread.currentThread().getContextClassLoader().getResource(htmlPath), () -> "Test resource not found: " + htmlPath);
        
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            builder.setEntityResolver(FSEntityResolver.instance());

            Document doc = builder.parse(htmlUrl.openStream());
            
            renderer.setDocument(doc, htmlUrl.toString());
            renderer.layout();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            renderer.createPDF(bos);
            return bos.toByteArray();
        }
        catch (DocumentException | IOException | SAXException | ParserConfigurationException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
