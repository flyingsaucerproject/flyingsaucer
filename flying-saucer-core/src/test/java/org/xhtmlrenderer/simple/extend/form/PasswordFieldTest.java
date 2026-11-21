package org.xhtmlrenderer.simple.extend.form;

import org.junit.jupiter.api.Test;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;

import javax.swing.JPasswordField;
import javax.swing.plaf.basic.BasicPasswordFieldUI;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static org.assertj.core.api.Assertions.assertThat;

class PasswordFieldTest {
    private static final String HTML = """
            <html>
              <body>
                <form>
                  <input type="password" name="p" value="secret"
                         style="color: #0000ff; background-color: #ffff00; width: 200px; padding: 4px 5px; border: 1px solid black"/>
                </form>
              </body>
            </html>""";

    @Test
    void passwordInputIsStyledLikeTextInput() {
        XHTMLPanel panel = new XHTMLPanel();
        panel.setDocumentFromString(HTML, null, new XhtmlNamespaceHandler());
        panel.setSize(400, 200);
        render(panel);

        assertThat(passwordFields(panel).toList()).singleElement().satisfies(field -> {
            assertThat(field.getForeground()).isEqualTo(Color.BLUE);
            assertThat(field.getBackground()).isEqualTo(Color.YELLOW);
            assertThat(field.getWidth()).isEqualTo(200 + 5 + 5);
            assertThat(field.getUI()).isInstanceOf(BasicPasswordFieldUI.class);
            assertThat(field.getPassword()).containsExactly('s', 'e', 'c', 'r', 'e', 't');
        });
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

    private static Stream<JPasswordField> passwordFields(Container container) {
        return Stream.of(container.getComponents()).flatMap(child -> Stream.concat(
                child instanceof JPasswordField field ? Stream.of(field) : Stream.empty(),
                child instanceof Container nested ? passwordFields(nested) : Stream.empty()
        ));
    }
}
