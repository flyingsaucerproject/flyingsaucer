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

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.*;
import org.w3c.dom.Element;
import org.xhtmlrenderer.css.parser.FSColor;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.util.*;

import com.itextpdf.text.Rectangle;

import java.io.IOException;

public class TextFormField extends AbstractFormField
{
  private static final String FIELD_TYPE = "Text";

  private static final int DEFAULT_SIZE = 15;

  private int _baseline;

  public TextFormField(LayoutContext c, BlockBox box, int cssWidth, int cssHeight)
  {
    initDimensions(c, box, cssWidth, cssHeight);

    float fontSize = box.getStyle().getFSFont(c).getSize2D();
    // FIXME: findbugs possible loss of precision, cf. int / (float)2
    _baseline = (int) (getHeight() / 2 + (fontSize * 0.3f));
  }

  protected void initDimensions(LayoutContext c, BlockBox box, int cssWidth, int cssHeight)
  {
    if (cssWidth != -1)
    {
      setWidth(cssWidth);
    }
    else
    {
      setWidth(c.getTextRenderer().getWidth(
          c.getFontContext(),
          box.getStyle().getFSFont(c),
          spaces(getSize(box.getElement()))));
    }

    if (cssHeight != -1)
    {
      setHeight(cssHeight);
    }
    else
    {
      setHeight((int) (box.getStyle().getLineHeight(c)));
    }
  }

  protected String getFieldType()
  {
    return FIELD_TYPE;
  }

  public void paint(RenderingContext c, ITextOutputDevice outputDevice, BlockBox box)
  {

    PdfWriter writer = outputDevice.getWriter();

    Element elem = box.getElement();

    Rectangle targetArea = outputDevice.createLocalTargetArea(c, box);
    TextField field = new TextField(writer, targetArea, getFieldName(outputDevice, elem));

    String value = getValue(elem);
    field.setText(value);

    try
    {
      PdfFormField formField = field.getTextField();
      createAppearance(c, outputDevice, box, formField, value);
      //TODO add max length back in
      if (isReadOnly(elem))
      {
        formField.setFieldFlags(PdfFormField.FF_READ_ONLY);
      }
      writer.addAnnotation(formField);
    } catch (IOException ioe)
    {
      System.out.println(ioe);
    } catch (DocumentException de)
    {
      System.out.println(de);
    }

  }

  private void createAppearance(RenderingContext c, ITextOutputDevice outputDevice, BlockBox box, PdfFormField field, String value)
  {
    PdfWriter writer = outputDevice.getWriter();
    ITextFSFont font = (ITextFSFont) box.getStyle().getFSFont(c);

    PdfContentByte cb = writer.getDirectContent();

    float width = outputDevice.getDeviceLength(getWidth());
    float height = outputDevice.getDeviceLength(getHeight());
    float fontSize = outputDevice.getDeviceLength(font.getSize2D());

    PdfAppearance tp = cb.createAppearance(width, height);
    PdfAppearance tp2 = (PdfAppearance) tp.getDuplicate();
    tp2.setFontAndSize(font.getFontDescription().getFont(), fontSize);

    FSColor color = box.getStyle().getColor();
    setFillColor(tp2, color);

    field.setDefaultAppearanceString(tp2);
    tp.beginVariableText();
    tp.saveState();
    tp.beginText();
    tp.setFontAndSize(font.getFontDescription().getFont(), fontSize);
    setFillColor(tp, color);
    tp.setTextMatrix(0, height / 2 - (fontSize * 0.3f));
    tp.showText(value);
    tp.endText();
    tp.restoreState();
    tp.endVariableText();
    field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, tp);
  }

  private int getSize(Element elem)
  {
    String sSize = elem.getAttribute("size");
    if (Util.isNullOrEmpty(sSize))
    {
      return DEFAULT_SIZE;
    }
    else
    {
      try
      {
        return Integer.parseInt(sSize.trim());
      } catch (NumberFormatException e)
      {
        return DEFAULT_SIZE;
      }
    }
  }

  private int getMaxLength(Element elem)
  {
    String sMaxLen = elem.getAttribute("maxlength");
    if (Util.isNullOrEmpty(sMaxLen))
    {
      return 0;
    }
    else
    {
      try
      {
        return Integer.parseInt(sMaxLen.trim());
      } catch (NumberFormatException e)
      {
        return 0;
      }
    }
  }

  protected String getValue(Element e)
  {
    String result = e.getAttribute("value");
    if (Util.isNullOrEmpty(result))
    {
      return "";
    }
    else
    {
      return result;
    }
  }

  public int getBaseline()
  {
    return _baseline;
  }

  public boolean hasBaseline()
  {
    return true;
  }
}
