package org.xhtmlrenderer.pdf;

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

public class EmptyReplacedElement implements ITextReplacedElement
{
  private int _width;
  private int _height;

  private Point _location = new Point(0, 0);

    public EmptyReplacedElement(int width, int height) {
        _width = width;
        _height = height;
    }

  public void paint(RenderingContext c, ITextOutputDevice outputDevice, BlockBox box)
  {

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
