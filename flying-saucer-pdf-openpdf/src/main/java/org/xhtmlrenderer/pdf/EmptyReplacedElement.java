package org.xhtmlrenderer.pdf;

import com.lowagie.text.pdf.PdfAcroForm;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfFormField;
import com.lowagie.text.pdf.PdfWriter;
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
    /*ISO-32000-1 defines the limit for a name in a PDF file to be at maximum 127 bytes.
     *Source(http://www.adobe.com/content/dam/Adobe/en/devnet/acrobat/pdfs/PDF32000_2008.pdf) 
     *  see Annex C § 2 Architectural limits "Table C.1" pages 649 and 650.
     *iText stores the hidden field value as a PDFName 
     */
    if (value.length() > 127) {
    	value = value.substring(0, 127);
    }
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
