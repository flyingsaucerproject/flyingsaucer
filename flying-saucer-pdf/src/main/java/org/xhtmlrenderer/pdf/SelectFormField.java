/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.pdf;

import com.lowagie.text.pdf.PdfAnnotation;
import com.lowagie.text.pdf.PdfAppearance;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfFormField;
import com.lowagie.text.pdf.PdfWriter;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.css.parser.FSColor;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.util.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.xhtmlrenderer.util.TextUtil.readTextContentOrNull;

public class SelectFormField extends AbstractFormField {
    private static final String FIELD_TYPE = "Select";

    private static final int EMPTY_SPACE_COUNT = 10;
    private static final int EXTRA_SPACE_COUNT = 4;

    private final List<Option> _options;

    private final int _baseline;

    public SelectFormField(LayoutContext c, BlockBox box, int cssWidth, int cssHeight) {
        _options = readOptions(box.getElement());
        initDimensions(c, box, cssWidth, cssHeight);

        float fontSize = box.getStyle().getFSFont(c).getSize2D();
        // FIXME: findbugs possible loss of precision, cf. int / (float)2
        _baseline = (int)(getHeight() / 2 + (fontSize * 0.3f));
    }

    private int getSelectedIndex() {
        int result = 0;

        int offset = 0;
        for (Iterator<Option> i = _options.iterator(); i.hasNext(); offset++) {
            Option option = i.next();
            if (option.isSelected()) {
                result = offset;
            }
        }

        return result;
    }

    private String[][] getPDFOptions() {
        List<Option> options = _options;
        String[][] result = new String[options.size()][];

        int offset = 0;
        for (Iterator<Option> i = options.iterator(); i.hasNext(); offset++) {
            Option option = i.next();
            result[offset] = new String[] { option.getValue(), option.getLabel() };
        }

        return result;
    }

    private int calcDefaultWidth(LayoutContext c, BlockBox box) {
        List<Option> options = _options;

        if (options.isEmpty()) {
            return c.getTextRenderer().getWidth(
                    c.getFontContext(),
                    box.getStyle().getFSFont(c),
                    spaces(EMPTY_SPACE_COUNT));
        } else {
            int maxWidth = 0;
            for (Option option : options) {
                String result = option.getLabel() + spaces(EXTRA_SPACE_COUNT);

                int width = c.getTextRenderer().getWidth(
                        c.getFontContext(),
                        box.getStyle().getFSFont(c),
                        result);

                if (width > maxWidth) {
                    maxWidth = width;
                }
            }

            return maxWidth;
        }
    }

    private List<Option> readOptions(Element e) {
        List<Option> result = new ArrayList<>();

        Node n = e.getFirstChild();
        while (n != null) {
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("option")) {
                Element optionElement = (Element) n;

                String label = readTextContentOrNull(optionElement);
                Attr valueAttribute = optionElement.getAttributeNode("value");
                String value = valueAttribute == null ? label : valueAttribute.getValue();

                if (label != null) {
                    Option option = new Option();
                    option.setLabel(label);
                    option.setValue(value);
                    if (isSelected(optionElement)) {
                        option.setSelected(true);
                    }
                    result.add(option);
                }
            }

            n = n.getNextSibling();
        }

        return result;
    }

    protected void initDimensions(LayoutContext c, BlockBox box, int cssWidth, int cssHeight) {
        if (cssWidth != -1) {
            setWidth(cssWidth);
        } else {
            setWidth(calcDefaultWidth(c, box));
        }

        if (cssHeight != -1) {
            setHeight(cssHeight);
        } else {
            setHeight((int) (box.getStyle().getLineHeight(c) * getSize(box.getElement())));
        }
    }

    private int getSize(Element elem) {
        try {
            String v = elem.getAttribute("size").trim();
            if (!v.isEmpty()) {
                int i = Integer.parseInt(v);
                if (i > 1) {
                    return i;
                }
            }
        } catch (NumberFormatException ignore) {
        }

        return 1;
    }

    protected boolean isMultiple(Element e) {
        return !Util.isNullOrEmpty(e.getAttribute("multiple"));
    }

    protected String getFieldType() {
        return FIELD_TYPE;
    }

    public void paint(RenderingContext c, ITextOutputDevice outputDevice, BlockBox box) {
        PdfWriter writer = outputDevice.getWriter();

        String[][] options = getPDFOptions();
        int selectedIndex = getSelectedIndex();

        PdfFormField field;

        /*
         * Comment out for now.  We need to draw an appropriate appearance for
         * this to work correctly.
         */
        /*
        if (isMultiple(box.getElement())) {
            field = PdfFormField.createList(writer, options, selectedIndex);
        } else {
            field = PdfFormField.createCombo(writer, false, options, selectedIndex);
        }
        */

        field = PdfFormField.createCombo(writer, false, options, selectedIndex);

        field.setWidget(outputDevice.createLocalTargetArea(c, box), PdfAnnotation.HIGHLIGHT_INVERT);
        field.setFieldName(getFieldName(outputDevice, box.getElement()));
        if (options.length > 0) {
            field.setValueAsString(options[selectedIndex][0]);
        }

        createAppearance(c, outputDevice, box, field);

        if (isReadOnly(box.getElement())) {
            field.setFieldFlags(PdfFormField.FF_READ_ONLY);
        }

        /*
        if (isMultiple(box.getElement())) {
            field.setFieldFlags(PdfFormField.FF_MULTISELECT);
        }
        */

        writer.addAnnotation(field);
    }

    private void createAppearance(
            RenderingContext c, ITextOutputDevice outputDevice,
            BlockBox box, PdfFormField field) {
        PdfWriter writer = outputDevice.getWriter();
        ITextFSFont font = (ITextFSFont)box.getStyle().getFSFont(c);

        PdfContentByte cb = writer.getDirectContent();

        float width = outputDevice.getDeviceLength(getWidth());
        float height = outputDevice.getDeviceLength(getHeight());
        float fontSize = outputDevice.getDeviceLength(font.getSize2D());

        PdfAppearance tp = cb.createAppearance(width, height);
        tp.setFontAndSize(font.getFontDescription().getFont(), fontSize);

        FSColor color = box.getStyle().getColor();
        setFillColor(tp, color);

        field.setDefaultAppearanceString(tp);
    }

    public int getBaseline() {
        return _baseline;
    }

    public boolean hasBaseline() {
        return true;
    }

    private static final class Option {
        private String _value;
        private String _label;
        private boolean _selected;

        public String getValue() {
            return _value;
        }

        public void setValue(String value) {
            _value = value;
        }

        public String getLabel() {
            return _label;
        }

        public void setLabel(String label) {
            _label = label;
        }

        public boolean isSelected() {
            return _selected;
        }

        public void setSelected(boolean selected) {
            _selected = selected;
        }
    }
}
