package org.xhtmlrenderer.pdf;

import com.itextpdf.text.pdf.PdfAcroForm;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.pdf.PdfWriter;
import org.w3c.dom.Element;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.RenderingContext;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: beck
 * Date: 11/4/11
 * Time: 12:42 PM
 */

public class EmptyReplacedElement extends AbstractFormField
{
  private static final String FIELD_TYPE = "Hidden";

  private int _width;
  private int _height;

  private Point _location = new Point(0, 0);

  public EmptyReplacedElement(int width, int height)
  {
    _width = width;
    _height = height;
  }

  public void paint(RenderingContext c, ITextOutputDevice outputDevice, BlockBox box)
  {
    PdfContentByte cb = outputDevice.getCurrentPage();

    PdfWriter writer = outputDevice.getWriter();

    PdfAcroForm acroForm = writer.getAcroForm();
    Element elem = box.getElement();
    String name = getFieldName(outputDevice, elem);
    String value = getValue(elem);
    acroForm.addHiddenField(name, value);


  }

  public int getIntrinsicWidth()
  {
    return _width;
  }

  public int getIntrinsicHeight()
  {
    return _height;
  }

  public Point getLocation()
  {
    return _location;
  }

  public void setLocation(int x, int y)
  {
    _location = new Point(0, 0);
  }

  protected String getFieldType()
  {
    return FIELD_TYPE;
  }

  public void detach(LayoutContext c)
  {
  }

  public boolean isRequiresInteractivePaint()
  {
    return false;
  }

  public boolean hasBaseline()
  {
    return false;
  }

  public int getBaseline()
  {
    return 0;
  }
}
