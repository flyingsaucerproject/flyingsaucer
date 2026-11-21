package org.xhtmlrenderer.simple.extend.form;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.function.Predicate;
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
        assertThat(textField(panel, "t").getHeight()).isEqualTo(30 + 2 + 2);
        assertThat(textField(panel, "p").getHeight()).isEqualTo(30 + 2 + 2);
        assertThat(component(panel, JScrollPane.class).getHeight()).isEqualTo(30 + 2 + 2);
        // look up by text: the textarea scrollbars may contain arrow buttons (e.g. in Metal look and feel)
        assertThat(component(panel, JButton.class, button -> button.getText().equals("s")).getHeight()).isEqualTo(30);
    }

    @ParameterizedTest
    @ValueSource(strings = {"text", "password"})
    void paddingInRelativeUnits(String type) {
        XHTMLPanel panel = render("""
                <input type="%s" value="x" style="font-size: 20px; padding: 1em 0.5em; border: 1px solid black"/>
                """.formatted(type));

        assertThat(textField(panel, "x").getInsets()).isEqualTo(new Insets(20, 10, 20, 10));
    }

    @ParameterizedTest
    @ValueSource(strings = {"text", "password"})
    void paddingInPixels(String type) {
        XHTMLPanel panel = render("""
                <input type="%s" value="x" style="padding: 7px 8px; border: 1px solid black"/>
                """.formatted(type));

        assertThat(textField(panel, "x").getInsets()).isEqualTo(new Insets(7, 8, 7, 8));
    }

    /**
     * Fields with the same style share one {@code CalculatedStyle}, including its cached padding,
     * which the first field resets after moving the padding into its Swing component.
     */
    @ParameterizedTest
    @ValueSource(strings = {"text", "password"})
    void fieldsWithSameStyle(String type) {
        String field = """
                <input type="%s" value="%s" style="padding: 16px; height: 30px; border: 1px solid black"/>""";
        XHTMLPanel panel = render(field.formatted(type, "first") + field.formatted(type, "second"));

        for (String value : new String[]{"first", "second"}) {
            JTextField textField = textField(panel, value);
            assertThat(textField.getInsets()).as(value).isEqualTo(new Insets(16, 16, 16, 16));
            assertThat(textField.getHeight()).as(value).isEqualTo(30 + 16 + 16);
        }
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

    private static JTextField textField(Container panel, String value) {
        return component(panel, JTextField.class, field -> field.getText().equals(value));
    }

    private static <T extends JComponent> T component(Container panel, Class<T> type) {
        return component(panel, type, component -> true);
    }

    private static <T extends JComponent> T component(Container panel, Class<T> type, Predicate<T> condition) {
        return components(panel)
                .filter(type::isInstance)
                .map(type::cast)
                .filter(condition)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No matching " + type.getSimpleName() + " in " + panel));
    }

    private static Stream<Component> components(Container container) {
        return Stream.of(container.getComponents()).flatMap(child -> Stream.concat(
                Stream.of(child),
                child instanceof Container nested ? components(nested) : Stream.empty()
        ));
    }
}
