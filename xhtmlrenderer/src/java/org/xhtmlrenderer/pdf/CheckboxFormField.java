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

import java.awt.Color;

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.parser.FSColor;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.RenderingContext;

import com.lowagie.text.pdf.PdfAnnotation;
import com.lowagie.text.pdf.PdfAppearance;
import com.lowagie.text.pdf.PdfBorderDictionary;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfFormField;
import com.lowagie.text.pdf.PdfWriter;

public class CheckboxFormField extends AbstractFormField {
	private static final String FIELD_TYPE = "Checkbox";

	public CheckboxFormField(LayoutContext c, BlockBox box, int cssWidth,
			int cssHeight) {
		initDimensions(c, box, cssWidth, cssHeight);
	}

	protected String getFieldType() {
		return FIELD_TYPE;
	}

	public void paint(RenderingContext c, ITextOutputDevice outputDevice, BlockBox box) {
		PdfContentByte cb = outputDevice.getCurrentPage();

		PdfWriter writer = outputDevice.getWriter();

		PdfFormField field = PdfFormField.createCheckBox(writer);

		Element e = box.getElement();
		String onValue = getValue(e);
		boolean checked = isChecked(e);

		float width = outputDevice.getDeviceLength(getWidth());
		float height = outputDevice.getDeviceLength(getHeight());

		FSColor color = box.getStyle().getColor();
		FSColor darker = box.getEffBackgroundColor(c).darkenColor();
		createAppearances(cb, field, onValue, width, height, true, color, darker);
		createAppearances(cb, field, onValue, width, height, false, color, darker);

		field.setWidget(outputDevice.createLocalTargetArea(c, box),
				PdfAnnotation.HIGHLIGHT_INVERT);
		field.setFieldName(getFieldName(outputDevice, e));

		field.setBorderStyle(new PdfBorderDictionary(0.0f, 0));

		field.setValueAsName(checked ? onValue : OFF_STATE);
		field.setAppearanceState(checked ? onValue : OFF_STATE);

		if (isReadOnly(e)) {
			field.setFieldFlags(PdfFormField.FF_READ_ONLY);
		}

		writer.addAnnotation(field);
	}

	private void createAppearances(PdfContentByte cb, PdfFormField field,
			String onValue, float width, float height, boolean normal,
			FSColor color, FSColor darker) {
		// XXX Should cache this by width and height, but they're small so
		// don't bother for now...
		PdfAppearance tpOff = cb.createAppearance(width, height);
		PdfAppearance tpOn = cb.createAppearance(width, height);

		setStrokeColor(tpOn, color);
		setStrokeColor(tpOff, color);

		float sLen = Math.min(width - reduce(width), height - reduce(height));

		if (!normal) {
		    setFillColor(tpOff, darker);
			tpOff.rectangle(0, 0, width, height);
			tpOff.fill();

			setFillColor(tpOn, darker);
			tpOn.rectangle(0, 0, width, height);
			tpOn.fill();
		}

		tpOn.moveTo(width / 2 - sLen / 2, height / 2 - sLen / 2);
		tpOn.lineTo(width / 2 + sLen / 2, height / 2 + sLen / 2);
		tpOn.moveTo(width / 2 - sLen / 2, height / 2 + sLen / 2);
		tpOn.lineTo(width / 2 + sLen / 2, height / 2 - sLen / 2);
		tpOn.stroke();

		if (normal) {
			field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, OFF_STATE, tpOff);
			field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, onValue, tpOn);
		} else {
			field.setAppearance(PdfAnnotation.APPEARANCE_DOWN, OFF_STATE, tpOff);
			field.setAppearance(PdfAnnotation.APPEARANCE_DOWN, onValue, tpOn);
		}
	}

	private float reduce(float value) {
		return Math.min(value, Math.max(2.0f, 0.08f * value));
	}

	public int getBaseline() {
		return 0;
	}

	public boolean hasBaseline() {
		return false;
	}
}
