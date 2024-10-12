package org.xhtmlrenderer.swing;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import static org.xhtmlrenderer.swing.Java2DRenderer.htmlAsImage;

public class QuotingExampleTest {
    private static final Logger log = LoggerFactory.getLogger(QuotingExampleTest.class);

    //currently we cannot display different quotes based on depth
    private static final String DOCUMENT =
            """
                    <html>
                      <head>
                        <style type='text/css'><![CDATA[
                          * { quotes: '"' '"' "'" "'" }
                          q:before { content: open-quote }
                          q:after { content: close-quote }
                          blockquote p:before     { content: open-quote }
                          blockquote p:after      { content: no-close-quote }
                          blockquote p.last:after { content: close-quote }
                        ]]></style>
                      </head>
                      <body>
                        <blockquote>
                          <p>This is just a test of the emergency <q>quoting</q> system.</p>
                          <p>This is only a test.</p>
                          <p class='last'>Thank you for your cooperation during this <q>test.</q></p>
                        </blockquote>
                      </body>
                    </html>
                    """;

    @Test
    public void exampleWithQuotes() throws Exception {
        BufferedImage image = htmlAsImage(DOCUMENT, 600);
        File result = new File("target/%s.png".formatted(getClass().getSimpleName()));
        ImageIO.write(image, "png", result);
        log.info("Generated image from html: {}", result.getAbsolutePath());
    }
}
