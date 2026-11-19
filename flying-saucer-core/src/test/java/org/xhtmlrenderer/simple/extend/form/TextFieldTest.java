package org.xhtmlrenderer.simple.extend.form;

import org.junit.jupiter.api.Test;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;

import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * See <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/782">issue 782</a>
 */
class TextFieldTest {
    private static final String HTML = """
            <html>
              <body>
                <form><input type="text" name="a" value="x"/></form>
              </body>
            </html>""";

    @Test
    void textInputIsRenderedAsTextField() {
        XHTMLPanel panel = new XHTMLPanel();
        panel.setDocumentFromString(HTML, null, new XhtmlNamespaceHandler());
        panel.setSize(400, 200);
        render(panel);

        List<JTextField> fields = new ArrayList<>();
        collectTextFields(panel, fields);
        assertThat(fields).singleElement().satisfies(field -> assertThat(field.getText()).isEqualTo("x"));
    }

    private static void render(XHTMLPanel panel) {
        BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            panel.paint(g);
        } finally {
            g.dispose();
        }
    }

    private static void collectTextFields(Container container, List<JTextField> fields) {
        for (Component child : container.getComponents()) {
            if (child instanceof JTextField field) {
                fields.add(field);
            }
            if (child instanceof Container nested) {
                collectTextFields(nested, fields);
            }
        }
    }
}
