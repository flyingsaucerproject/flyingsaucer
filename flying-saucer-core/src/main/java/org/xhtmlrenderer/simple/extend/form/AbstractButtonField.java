package org.xhtmlrenderer.simple.extend.form;

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.parser.FSColor;
import org.xhtmlrenderer.css.parser.FSRGBColor;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.FSDerivedValue;
import org.xhtmlrenderer.css.style.derived.BorderPropertySet;
import org.xhtmlrenderer.css.style.derived.LengthValue;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.XhtmlForm;

import javax.swing.*;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

abstract class AbstractButtonField<T extends JButton> extends InputField<JButton> {

    protected AbstractButtonField(Element e, XhtmlForm form, LayoutContext context, BlockBox box) {
        super(e, form, context, box);
    }

    protected void applyComponentStyle(T button) {

        super.applyComponentStyle(button);

        CalculatedStyle style = getBox().getStyle();
        BorderPropertySet border = style.getBorder(null);
        boolean disableOSBorder = (border.leftStyle() != null && border.rightStyle() != null || border.topStyle() != null || border.bottomStyle() != null);

        FSColor backgroundColor = style.getBackgroundColor();

        //if a border is set or a background color is set, then use a special JButton with the BasicButtonUI.
        if (disableOSBorder || backgroundColor instanceof FSRGBColor) {
            //when background color is set, need to use the BasicButtonUI, certainly when using XP l&f
            BasicButtonUI ui = new BasicButtonUI();
            button.setUI(ui);

            if (backgroundColor instanceof FSRGBColor rgb) {
                button.setBackground(new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue()));
            }

            if (disableOSBorder)
                button.setBorder(new BasicBorders.MarginBorder());
            else
                button.setBorder(BasicBorders.getButtonBorder());
        }

        button.setMargin(style.padding().withDefaults(new Insets(2, 12, 2, 12)));

        RectPropertySet padding = style.getCachedPadding();
        padding.reset();

        FSDerivedValue widthValue = style.valueByName(CSSName.WIDTH);
        if (widthValue instanceof LengthValue)
            intrinsicWidth = getBox().getContentWidth();

        FSDerivedValue heightValue = style.valueByName(CSSName.HEIGHT);
        if (heightValue instanceof LengthValue)
            intrinsicHeight = getBox().getHeight();
    }
}
