package org.xhtmlrenderer.swing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static java.awt.Color.RED;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * See <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/500">issue 500</a>
 */
class DetailsToggleListenerTest {
    private static final String HTML = """
            <html>
              <head><style>
                body { margin: 0; background-color: white; }
                summary { height: 20px; }
                .content { background-color: red; height: 50px; }
              </style></head>
              <body>
                <details>
                  <summary>Toggle <a href="other.html">link</a></summary>
                  <div class="content" id="hidden">Hidden content</div>
                </details>
              </body>
            </html>""";

    private final XHTMLPanel panel = new XHTMLPanel();
    private final DetailsToggleListener listener = new DetailsToggleListener();
    private Element details;

    @BeforeEach
    void setUp() {
        panel.setDocumentFromString(HTML, null, new XhtmlNamespaceHandler());
        panel.setSize(300, 200);
        details = (Element) requireNonNull(panel.getDocument()).getElementsByTagName("details").item(0);
    }

    @Test
    void clickOnSummaryOpensAndClosesDetails() {
        assertThat(countRedPixels(render())).isZero();

        listener.onMouseUp(panel, summaryBox());
        assertThat(details.hasAttribute("open")).isTrue();
        assertThat(countRedPixels(render())).isPositive();

        listener.onMouseUp(panel, summaryBox());
        assertThat(details.hasAttribute("open")).isFalse();
        assertThat(countRedPixels(render())).isZero();
    }

    @Test
    void collapsingDetailsRemovesIdsOfHiddenContent() {
        listener.onMouseUp(panel, summaryBox());
        render();
        assertThat(panel.getSharedContext().getBoxById("hidden")).isNotNull();

        listener.onMouseUp(panel, summaryBox());
        render();
        assertThat(panel.getSharedContext().getBoxById("hidden")).isNull();
    }

    @Test
    void clickOnLinkInsideSummaryDoesNotToggleDetails() {
        Element link = (Element) details.getElementsByTagName("a").item(0);

        assertThat(DetailsToggleListener.findToggledDetails(new XhtmlNamespaceHandler(), link)).isNull();
    }

    @Test
    void clickOutsideSummaryDoesNotToggleDetails() {
        Element body = (Element) details.getParentNode();

        assertThat(DetailsToggleListener.findToggledDetails(new XhtmlNamespaceHandler(), body)).isNull();
        assertThat(DetailsToggleListener.findToggledDetails(new XhtmlNamespaceHandler(), details)).isNull();
    }

    private Box summaryBox() {
        render();
        Box box = requireNonNull(panel.find(10, 10));
        assertThat(requireNonNull(box.getElement()).getTagName()).isEqualTo("summary");
        return box;
    }

    private BufferedImage render() {
        BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            panel.paint(g);
        } finally {
            g.dispose();
        }
        return image;
    }

    private static int countRedPixels(BufferedImage image) {
        int count = 0;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (image.getRGB(x, y) == RED.getRGB()) {
                    count++;
                }
            }
        }
        return count;
    }
}
