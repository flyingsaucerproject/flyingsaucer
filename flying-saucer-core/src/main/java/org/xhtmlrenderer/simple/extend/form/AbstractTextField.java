package org.xhtmlrenderer.simple.extend.form;

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.FSDerivedValue;
import org.xhtmlrenderer.css.style.derived.LengthValue;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.XhtmlForm;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.plaf.basic.BasicTextUI;
import java.awt.*;

/**
 * Common base for single-line text inputs ({@code <input type="text">}, {@code <input type="password">}),
 * applying the CSS font, colors, padding, border and size to the Swing text field.
 */
abstract class AbstractTextField<T extends JTextField> extends InputField<T> {
    protected AbstractTextField(Element e, XhtmlForm form, LayoutContext context, BlockBox box) {
        super(e, form, context, box);
    }

    @Override
    protected void applyComponentStyle(JComponent component) {
        super.applyComponentStyle(component);

        // called from create(), before the component is set
        JTextField field = (JTextField) component;
        CalculatedStyle style = getBox().getStyle();
        RectPropertySet padding = style.getCachedPadding();
        Insets margin = style.padding().withDefaults(new Insets(2, 3, 2, 3));

        //if a border is set or a background color is set, then use a special JButton with the BasicButtonUI.
        if (style.disableOSBorder()) {
            //when background color is set, need to use the BasicButtonUI, certainly when using XP l&f
            field.setUI(createBasicUI());
            Border fieldBorder = BorderFactory.createEmptyBorder(margin.top, margin.left, margin.bottom, margin.right);
            field.setBorder(fieldBorder);
        }
        else {
            field.setMargin(margin);
        }

        Integer cssHeight = cssContentHeight();
        if (cssHeight != null) {
            intrinsicHeight = cssHeight + margin.top + margin.bottom;
        }

        padding.reset();

        FSDerivedValue widthValue = style.valueByName(CSSName.WIDTH);
        if (widthValue instanceof LengthValue) {
            intrinsicWidth = getBox().getContentWidth() + margin.left + margin.right;
        }
    }

    /**
     * UI used instead of the look-and-feel one when the OS border is disabled (CSS border or background is set).
     */
    protected BasicTextUI createBasicUI() {
        return new BasicTextFieldUI();
    }
}
