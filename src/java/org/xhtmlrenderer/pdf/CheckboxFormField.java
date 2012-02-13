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
import java.io.IOException;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.BaseColor;
import org.w3c.dom.Element;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.RenderingContext;

public class CheckboxFormField extends AbstractFormField
{
  private static final String FIELD_TYPE = "Checkbox";

  public CheckboxFormField(LayoutContext c, BlockBox box, int cssWidth, int cssHeight)
  {
    initDimensions(c, box, cssWidth, cssHeight);
  }

  protected String getFieldType()
  {
    return FIELD_TYPE;
  }

  public void paint(RenderingContext c, ITextOutputDevice outputDevice, BlockBox box)
  {
    PdfContentByte cb = outputDevice.getCurrentPage();

    PdfWriter writer = outputDevice.getWriter();
    Element elm = box.getElement();

    Rectangle targetArea = outputDevice.createLocalTargetArea(c, box);
    String onValue = getValue(elm);

    RadioCheckField field = new RadioCheckField(writer, targetArea, getFieldName(outputDevice, elm), onValue);


    field.setChecked(isChecked(elm));
    field.setCheckType(RadioCheckField.TYPE_CHECK);
    field.setBorderStyle(PdfBorderDictionary.STYLE_SOLID);
    //TODO Consider if we can get some more correct color
    field.setBorderColor(BaseColor.BLACK);

    field.setBorderWidth(BaseField.BORDER_WIDTH_THIN);

    try
    {
      PdfFormField formField = field.getCheckField();
      if (isReadOnly(elm))
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

  public int getBaseline()
  {
    return 0;
  }

  public boolean hasBaseline()
  {
    return false;
  }
}
