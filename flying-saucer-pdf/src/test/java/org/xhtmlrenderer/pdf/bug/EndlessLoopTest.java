package org.xhtmlrenderer.pdf.bug;

import com.codeborne.pdftest.PDF;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.xhtmlrenderer.pdf.Html2Pdf;

import java.net.URL;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;

public class EndlessLoopTest {

  @Test
  @Timeout(value = 3, unit = SECONDS)
  public void wordwrap() {
    URL htmlUrl = requireNonNull(getClass().getResource("EndlessLoopTest_wordwrap.html"));
    byte[] pdf = Html2Pdf.fromUrl(htmlUrl);
    assertThat(new PDF(pdf)).containsText(
            "floated",
            "word wrapped"
    );
  }
}
