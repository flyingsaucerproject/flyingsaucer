package org.xhtmlrenderer.pdf.bug;

import org.junit.Test;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.net.URL;

public class EndlessLoopTest {

  @Test(timeout = 3000L)
  public void testWordwrap() throws Exception {
    URL htmlUrl = getClass().getResource("EndlessLoopTest_wordwrap.html");
    File htmlFile = new File(htmlUrl.toURI());
    ITextRenderer renderer = new ITextRenderer();
    renderer.setDocument(htmlFile);
    renderer.layout();
  }
}
