/*
 * {{{ header & license
 * Copyright (c) 2008 elbart0 at free.fr (submitted via email)
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

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.extend.FSCanvas;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.swing.AWTFSImage;
import org.xhtmlrenderer.swing.SwingReplacedElement;
import org.xhtmlrenderer.swing.SwingReplacedElementFactory;
import org.xhtmlrenderer.util.XRLog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.logging.Level;


/**
 * Sample for handling image maps in XHTML, as replaced elements.
 *
 * Sample is incomplete in current state and meant as a starting point for future work.
 */
public class ImageMapReplacedElementFactory extends SwingReplacedElementFactory {
   private final ImageMapListener listener;
   private static final String IMG_USEMAP_ATTR = "usemap";
   private static final String MAP_ELT = "map";
   private static final String MAP_NAME_ATTR = "name";
   private static final String AREA_ELT = "area";
   private static final String AREA_SHAPE_ATTR = "shape";
   private static final String AREA_COORDS_ATTR = "coords";
   private static final String AREA_HREF_ATTR = "href";
   private static final String RECT_SHAPE = "rect";
   private static final String RECTANGLE_SHAPE = "rectangle";
   private static final String CIRC_SHAPE = "circ";
   private static final String CIRCLE_SHAPE = "circle";
   private static final String POLY_SHAPE = "poly";
   private static final String POLYGON_SHAPE = "polygon";

   public ImageMapReplacedElementFactory(ImageMapListener listener) {
       super(null);
       if (null == listener) {
         throw new IllegalArgumentException("listener required");
      }
      this.listener = listener;
   }

   public ReplacedElement createReplacedElement(LayoutContext context, BlockBox box, UserAgentCallback uac, int cssWidth, int cssHeight) {
      Element e = box.getElement();
      if (e == null) {
         return null;
      } else if (context.getNamespaceHandler().isImageElement(e)) {
         final String usemapAttr = context.getNamespaceHandler().getAttributeValue(e, IMG_USEMAP_ATTR);
         if (isNotBlank(usemapAttr)) {
            final ReplacedElement re = replaceImageMap(uac, context, e, usemapAttr, cssWidth, cssHeight);
            if (context.isInteractive() && re instanceof SwingReplacedElement) {
                FSCanvas canvas = context.getCanvas();
                if (canvas instanceof JComponent) {
                    ((JComponent) canvas).add(((SwingReplacedElement) re).getJComponent());
                }
            }
            return re;
         } else {
            return replaceImage(uac, context, e, cssWidth, cssHeight);
         }
      } else {
         return null;
      }
   }

    private boolean isNotBlank(String _v) {
        if (_v == null || _v.length() == 0) {
            return false;
        }
        for (int i = 0; i < _v.length(); i++) {
             if (Character.isWhitespace(_v.charAt(i))) continue;
            return false;
        }
        return true;
    }

    // See SwingReplacedElementFactory#replaceImage
   protected ReplacedElement replaceImageMap(UserAgentCallback uac, LayoutContext context, Element elem, String usemapAttr, int cssWidth, int cssHeight) {
      ReplacedElement re;
      // lookup in cache, or instantiate
      re = lookupImageReplacedElement(elem, "");
      if (re == null) {
         Image im = null;
         String imageSrc = context.getNamespaceHandler().getImageSourceURI(elem);
         if (imageSrc == null || imageSrc.length() == 0) {
            XRLog.layout(Level.WARNING, "No source provided for img element.");
            re = newIrreplaceableImageElement(cssWidth, cssHeight);
         } else {
            FSImage fsImage = uac.getImageResource(imageSrc).getImage();
            if (fsImage != null) {
               im = ((AWTFSImage) fsImage).getImage();
            }

            if (im != null) {
               final String mapName = usemapAttr.substring(1);
               Node map = elem.getOwnerDocument().getElementById(mapName);
               if (null == map) {
                  final NodeList maps = elem.getOwnerDocument().getElementsByTagName(MAP_ELT);
                  for (int i = 0; i < maps.getLength(); i++) {
                      String mapAttr = ImageMapReplacedElement.getAttribute(maps.item(i).getAttributes(), MAP_NAME_ATTR);
                      if (areEqual(mapName, mapAttr)) {
                        map = maps.item(i);
                        break;
                     }
                  }
                  if (null == map) {
                     XRLog.layout(Level.INFO, "No map named: '" + mapName + "'");
                  }
               }
               re = new ImageMapReplacedElement(im, map, cssWidth, cssHeight, listener);
            } else {
               re = newIrreplaceableImageElement(cssWidth, cssHeight);
            }
         }
         storeImageReplacedElement(elem, re, "", -1, -1);
      }
      return re;
   }

    private static boolean areEqual(String str1, String str2) {
        return (str1 == null && str2 == null) || (str1 != null && str1.equals(str2));
    }
    private static boolean areEqualIgnoreCase(String str1, String str2) {
        return (str1 == null && str2 == null) || (str1 != null && str1.equalsIgnoreCase(str2));
    }

    private static class ImageMapReplacedElement extends SwingReplacedElement {
      private final Map areas;

      public ImageMapReplacedElement(Image image, Node map, int targetWidth, int targetHeight, final ImageMapListener listener) {
         super(create(image, targetWidth, targetHeight));
         areas = parseMap(map);
         getJComponent().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
               final Point point = e.getPoint();
                final Set set = areas.entrySet();
                for (Iterator iterator = set.iterator(); iterator.hasNext();) {
                    Map.Entry entry = (Map.Entry) iterator.next();

                    if (((Shape) entry.getKey()).contains(point)) {
                        listener.areaClicked(new ImageMapEvent(this, (String) entry.getValue()));
                    }
                }
            }

            public void mouseExited(MouseEvent e) {
               getJComponent().setCursor(Cursor.getDefaultCursor());
            }
         });
         getJComponent().addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
               final JComponent c = getJComponent();
               final Point point = e.getPoint();
                final Set set = areas.entrySet();
                for (Iterator iterator = set.iterator(); iterator.hasNext();) {
                    Map.Entry entry = (Map.Entry) iterator.next();

                    if (((Shape) entry.getKey()).contains(point)) {
                        updateCursor(c, Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        return;
                    }
                }
               updateCursor(c, Cursor.getDefaultCursor());
            }
         });
      }

      private static void updateCursor(JComponent c, Cursor cursor) {
         if (!c.getCursor().equals(cursor)) {
            c.setCursor(cursor);
         }
      }

      private static Map parseMap(Node map) {
         if (null == map) {
            return Collections.emptyMap();
         } else if (map.hasChildNodes()) {
            final NodeList children = map.getChildNodes();
            final Map areas = new HashMap(children.getLength());
            for (int i = 0; i < children.getLength(); i++) {
               final Node area = children.item(i);
               if (areEqualIgnoreCase(AREA_ELT, area.getNodeName())) {
                  if (area.hasAttributes()) {
                     final NamedNodeMap attrs = area.getAttributes();
                     final String shapeAttr = getAttribute(attrs, AREA_SHAPE_ATTR);
                     final String[] coords = getAttribute(attrs, AREA_COORDS_ATTR).split(",");
                     final String href = getAttribute(attrs, AREA_HREF_ATTR);
                     if (areEqualIgnoreCase(RECT_SHAPE, shapeAttr) || areEqualIgnoreCase(RECTANGLE_SHAPE, shapeAttr)) {
                        final Shape shape = getCoords(coords, 4);
                        if (null != shape) {
                           areas.put(shape, href);
                        }
                     } else if (areEqualIgnoreCase(CIRC_SHAPE, shapeAttr) || areEqualIgnoreCase(CIRCLE_SHAPE, shapeAttr)) {
                        final Shape shape = getCoords(coords, 3);
                        if (null != shape) {
                           areas.put(shape, href);
                        }
                     } else if (areEqualIgnoreCase(POLY_SHAPE, shapeAttr) || areEqualIgnoreCase(POLYGON_SHAPE, shapeAttr)) {
                        final Shape shape = getCoords(coords, -1);
                        if (null != shape) {
                           areas.put(shape, href);
                        }
                     } else {
                        if (XRLog.isLoggingEnabled()) {
                           XRLog.layout(Level.INFO, "Unsupported shape: '" + shapeAttr + "'");
                        }
                     }
                  }
               }
            }
            return areas;
         } else {
            return Collections.emptyMap();
         }
      }

      private static String getAttribute(NamedNodeMap attrs, String attrName) {
         final Node node = attrs.getNamedItem(attrName);
         return null == node ? null : node.getNodeValue();
      }

      private static Shape getCoords(String[] coordValues, int length) {
         if ((-1 == length && 0 == coordValues.length % 2) || length == coordValues.length) {
            int[] coords = new int[coordValues.length];
            int i = 0;
             for (int i1 = 0; i1 < coordValues.length; i1++) {
                 String coord = coordValues[i1];
                 try {
                     coords[i++] = Integer.parseInt(coord.trim());
                 } catch (NumberFormatException e) {
                     XRLog.layout(Level.WARNING, "Error while parsing shape coords", e);
                     return null;
                 }
             }
            if (4 == length) {
               return new Rectangle2D.Float(coords[0], coords[1], coords[2] - coords[0], coords[3] - coords[1]);
            } else if (3 == length) {
               final int radius = coords[2];
               return new Ellipse2D.Float(coords[0] - radius, coords[1] - radius, radius * 2, radius * 2);
            } else if (-1 == length) {
               final int npoints = coords.length / 2;
               final int[] xpoints = new int[npoints];
               final int[] ypoints = new int[npoints];
               for (int c = 0, p = 0; p < npoints; p++) {
                  xpoints[p] = coords[c++];
                  ypoints[p] = coords[c++];
               }
               return new Polygon(xpoints, ypoints, npoints);
            } else {
               XRLog.layout(Level.INFO, "Unsupported shape: '" + length + "'");
               return null;
            }
         } else {
            return null;
         }
      }

      private static JComponent create(Image image, int targetWidth, int targetHeight) {
         final JLabel component = new JLabel(new ImageIcon(image));
         component.setSize(component.getPreferredSize());
         return component;
      }
   }
}
