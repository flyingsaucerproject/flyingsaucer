package org.xhtmlrenderer.pdf.bug;

import com.codeborne.pdftest.PDF;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static java.util.concurrent.TimeUnit.SECONDS;

public class EndlessLoopTest {

  @Test
  @Timeout(value = 3, unit = SECONDS)
  public void wordwrap() throws Exception {
    URL htmlUrl = getClass().getResource("EndlessLoopTest_wordwrap.html");
    File htmlFile = new File(htmlUrl.toURI());
    ITextRenderer renderer = new ITextRenderer();
    renderer.setDocument(htmlFile);
    renderer.layout();

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    renderer.createPDF(bos);    
    assertThat(new PDF(bos.toByteArray())).containsText(
            "floated",
            "word wrapped"
    );
  }
}
