package org.xhtmlrenderer.simple.extend.form;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * CSS {@code height} and {@code padding} of form fields rendered in {@link XHTMLPanel}.
 */
class FormFieldSizeTest {
    private static final String FIELDS = """
            <input type="text" value="t" style="height: 30px; border: 1px solid black"/>
            <input type="password" value="p" style="height: 30px; border: 1px solid black"/>
            <textarea style="height: 30px; border: 1px solid black">a</textarea>
            <input type="submit" value="s" style="height: 30px"/>""";

    /**
     * Inline-block and float are shrink-to-fit: the field is created while measuring them, before the box height is known.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "<div>%s</div>",
            "<span style='display: inline-block'>%s</span>",
            "<div style='float: left'>%s</div>"
    })
    void cssHeight(String wrapper) {
        XHTMLPanel panel = render(wrapper.formatted(FIELDS));

        // text fields add their default 2px top/bottom padding (moved into the Swing component) to the CSS height
        assertThat(component(panel, JTextField.class, false).getHeight()).isEqualTo(30 + 2 + 2);
        assertThat(component(panel, JPasswordField.class, true).getHeight()).isEqualTo(30 + 2 + 2);
        assertThat(component(panel, JScrollPane.class, false).getHeight()).isEqualTo(30 + 2 + 2);
        assertThat(component(panel, JButton.class, false).getHeight()).isEqualTo(30);
    }

    @ParameterizedTest
    @ValueSource(strings = {"text", "password"})
    void paddingInRelativeUnits(String type) {
        XHTMLPanel panel = render("""
                <input type="%s" value="x" style="font-size: 20px; padding: 1em 0.5em; border: 1px solid black"/>
                """.formatted(type));

        JTextField field = component(panel, JTextField.class, type.equals("password"));
        assertThat(field.getInsets()).isEqualTo(new Insets(20, 10, 20, 10));
    }

    @ParameterizedTest
    @ValueSource(strings = {"text", "password"})
    void paddingInPixels(String type) {
        XHTMLPanel panel = render("""
                <input type="%s" value="x" style="padding: 7px 8px; border: 1px solid black"/>
                """.formatted(type));

        JTextField field = component(panel, JTextField.class, type.equals("password"));
        assertThat(field.getInsets()).isEqualTo(new Insets(7, 8, 7, 8));
    }

    private static XHTMLPanel render(String body) {
        XHTMLPanel panel = new XHTMLPanel();
        panel.setDocumentFromString("<html><body><form>" + body + "</form></body></html>", null, new XhtmlNamespaceHandler());
        panel.setSize(800, 400);
        BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            panel.paint(g);
        } finally {
            g.dispose();
        }
        return panel;
    }

    private static <T extends JComponent> T component(Container panel, Class<T> type, boolean password) {
        return components(panel)
                .filter(type::isInstance)
                .filter(c -> c instanceof JPasswordField == password)
                .map(type::cast)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No " + type.getSimpleName() + " in " + panel));
    }

    private static Stream<Component> components(Container container) {
        return Stream.of(container.getComponents()).flatMap(child -> Stream.concat(
                Stream.of(child),
                child instanceof Container nested ? components(nested) : Stream.empty()
        ));
    }
}
