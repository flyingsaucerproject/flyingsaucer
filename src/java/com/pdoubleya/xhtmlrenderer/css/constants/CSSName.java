/*
 * {{{ header & license
 * CSSName.java
 * Copyright (c) 2004 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package com.pdoubleya.xhtmlrenderer.css.constants;

import java.lang.reflect.*;

import java.util.*;

/**
 * Used for CSS2-related constants
 *
 * @author    Patrick Wright
 * @created   August 2, 2004
 */
public final class CSSName {

  /** Constant string for CSS2 property. */
  public final static String COLOR = "color";

  /** Constant string for CSS2 property. */
  public final static String BACKGROUND_SHORTHAND = "background";// TODO: shorthand

  /** Constant string for CSS2 property. */
  public final static String BACKGROUND_COLOR = "background-color";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String BACKGROUND_IMAGE = "background-image";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String BACKGROUND_REPEAT = "background-repeat";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String BACKGROUND_ATTACHMENT = "background-attachment";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String BACKGROUND_POSITION = "background-position";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String BORDER_COLLAPSE = "border-collapse";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String BORDER_SPACING = "border-spacing";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String BOTTOM = "bottom";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String CAPTION_SIDE = "caption-side";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String CLEAR = "clear";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String CLIP = "clip";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String CONTENT = "content";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String COUNTER_INCREMENT = "counter-increment";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String COUNTER_RESET = "counter-reset";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String CURSOR = "cursor";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String DIRECTION = "direction";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String DISPLAY = "display";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String EMPTY_CELLS = "empty-cells";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String FLOAT = "float";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String FONT_SHORTHAND = "font";// TODO: shorthand

  /** Constant string for CSS2 property. */
  public final static String FONT_STYLE = "font-style";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String FONT_VARIANT = "font-variant";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String FONT_WEIGHT = "font-weight";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String FONT_SIZE = "font-size";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String LINE_HEIGHT = "line-height";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String FONT_FAMILY = "font-family";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String FONT_SIZE_ADJUST = "font-size-adjust";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String FONT_STRETCH = "font-stretch";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String HEIGHT = "height";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String LEFT = "left";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String LETTER_SPACING = "letter-spacing";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String LIST_STYLE_SHORTHAND = "list-style";// TODO: shorthand

  /** Constant string for CSS2 property. */
  public final static String LIST_STYLE_TYPE = "list-style-type";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String LIST_STYLE_POSITION = "list-style-position";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String LIST_STYLE_IMAGE = "list-style-image";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String MARKER_OFFSET = "marker-offset";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String MARKS = "marks";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String MAX_HEIGHT = "max-height";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String MAX_WIDTH = "max-width";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String MIN_HEIGHT = "min-height";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String MIN_WIDTH = "min-width";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String ORPHANS = "orphans";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String OUTLINE_SHORTHAND = "outline";// TODO: shorthand

  /** Constant string for CSS2 property. */
  public final static String OUTLINE_COLOR = "outline-color";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String OUTLINE_STYLE = "outline-style";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String OUTLINE_WIDTH = "outline-width";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String OVERFLOW = "overflow";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String PAGE = "page";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String PAGE_BREAK_AFTER = "page-break-after";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String PAGE_BREAK_BEFORE = "page-break-before";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String PAGE_BREAK_INSIDE = "page-break-inside";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String POSITION = "position";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String QUOTES = "quotes";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String RIGHT = "right";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String SIZE = "size";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String TABLE_LAYOUT = "table-layout";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String TEXT_ALIGN = "text-align";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String TEXT_DECORATION = "text-decoration";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String TEXT_INDENT = "text-indent";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String TEXT_SHADOW = "text-shadow";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String TEXT_TRANSFORM = "text-transform";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String TOP = "top";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String UNICODE_BIDI = "unicode-bidi";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String VERTICAL_ALIGN = "vertical-align";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String VISIBILITY = "visibility";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String WHITE_SPACE = "white-space";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String WIDOWS = "widows";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String WIDTH = "width";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String WORD_SPACING = "word-spacing";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String Z_INDEX = "z-index";// TODO: just added, handle property

  /** Constant string for CSS2 property. */
  public final static String BORDER_SHORTHAND = "border";
  /** Constant string for CSS2 property. */
  public final static String BORDER_TOP_SHORTHAND = "border-top";
  /** Constant string for CSS2 property. */
  public final static String BORDER_RIGHT_SHORTHAND = "border-right";
  /** Constant string for CSS2 property. */
  public final static String BORDER_BOTTOM_SHORTHAND = "border-bottom";
  /** Constant string for CSS2 property. */
  public final static String BORDER_LEFT_SHORTHAND = "border-left";

  /** Constant string for CSS2 property. */
  public final static String BORDER_COLOR_SHORTHAND = "border-color";
  /** Constant string for CSS2 property. */
  public final static String BORDER_COLOR_TOP = "border-top-color";
  /** Constant string for CSS2 property. */
  public final static String BORDER_COLOR_RIGHT = "border-right-color";
  /** Constant string for CSS2 property. */
  public final static String BORDER_COLOR_BOTTOM = "border-bottom-color";
  /** Constant string for CSS2 property. */
  public final static String BORDER_COLOR_LEFT = "border-left-color";

  /** Constant string for CSS2 property. */
  public final static String BORDER_STYLE_SHORTHAND = "border-style";
  /** Constant string for CSS2 property. */
  public final static String BORDER_STYLE_TOP = "border-top-style";
  /** Constant string for CSS2 property. */
  public final static String BORDER_STYLE_RIGHT = "border-right-style";
  /** Constant string for CSS2 property. */
  public final static String BORDER_STYLE_BOTTOM = "border-bottom-style";
  /** Constant string for CSS2 property. */
  public final static String BORDER_STYLE_LEFT = "border-left-style";

  /** Constant string for CSS2 property. */
  public final static String BORDER_WIDTH_SHORTHAND = "border-width";

  /** Constant string for CSS2 property. */
  public final static String BORDER_WIDTH_TOP = "border-top-width";
  /** Constant string for CSS2 property. */
  public final static String BORDER_WIDTH_RIGHT = "border-right-width";
  /** Constant string for CSS2 property. */
  public final static String BORDER_WIDTH_BOTTOM = "border-bottom-width";
  /** Constant string for CSS2 property. */
  public final static String BORDER_WIDTH_LEFT = "border-left-width";

  /** Constant string for CSS2 property. */
  public final static String MARGIN_SHORTHAND = "margin";
  /** Constant string for CSS2 property. */
  public final static String MARGIN_TOP = "margin-top";
  /** Constant string for CSS2 property. */
  public final static String MARGIN_RIGHT = "margin-right";
  /** Constant string for CSS2 property. */
  public final static String MARGIN_BOTTOM = "margin-bottom";
  /** Constant string for CSS2 property. */
  public final static String MARGIN_LEFT = "margin-left";

  /** Constant string for CSS2 property. */
  public final static String PADDING_SHORTHAND = "padding";
  /** Constant string for CSS2 property. */
  public final static String PADDING_TOP = "padding-top";
  /** Constant string for CSS2 property. */
  public final static String PADDING_RIGHT = "padding-right";
  /** Constant string for CSS2 property. */
  public final static String PADDING_BOTTOM = "padding-bottom";
  /** Constant string for CSS2 property. */
  public final static String PADDING_LEFT = "padding-left";
  /** Constant string for CSS2 property. */
  private final static List ALL_PROPERTY_NAMES;// static block at bottom of class
  /** Constant string for CSS2 property. */
  private final static List ALL_INHERITABLE;// static block at bottom of class


  /**
   * Description of the Method
   *
   * @return   Returns
   */
  public final static Iterator allCSS2PropertyNames() {
    return ALL_PROPERTY_NAMES.iterator();
  }


  /**
   * Description of the Method
   *
   * @param propName  PARAM
   * @return          Returns
   */
  public final static boolean propertyInherits( String propName ) {
    return ALL_INHERITABLE.contains( propName );
  }

  static {
    ALL_PROPERTY_NAMES = new ArrayList();
    try {
      Field fields[] = CSSName.class.getFields();
      for ( int i = 0; i < fields.length; i++ ) {
        Field f = fields[i];
        int mod = f.getModifiers();
        if ( Modifier.isFinal( mod ) && Modifier.isStatic( mod ) && Modifier.isPublic( mod ) && f.getType() == String.class ) {
          ALL_PROPERTY_NAMES.add( (String)f.get( null ) );
        }
      }
    } catch ( Exception ex ) {
      ex.printStackTrace();
    }

    ALL_INHERITABLE = new ArrayList();
    Iterator iter = ALL_PROPERTY_NAMES.iterator();

    // there has got to be a better way!
    while ( iter.hasNext() ) {
      String prop = (String)iter.next();
      // margins, padding, border never inherit
      if ( prop.startsWith( "margin" ) ) {
        continue;
      }
      if ( prop.startsWith( "padding" ) ) {
        continue;
      }
      if ( prop.startsWith( "border" ) ) {
        continue;
      }
      if ( prop.startsWith( "display" ) ) {
        continue;
      }
      if ( prop.startsWith( "position" ) ) {
        continue;
      }
      if ( prop.startsWith( "top" ) ) {
        continue;
      }
      if ( prop.startsWith( "right" ) ) {
        continue;
      }
      if ( prop.startsWith( "bottom" ) ) {
        continue;
      }
      if ( prop.startsWith( "left" ) ) {
        continue;
      }
      if ( prop.startsWith( "float" ) ) {
        continue;
      }
      if ( prop.startsWith( "clear" ) ) {
        continue;
      }
      if ( prop.startsWith( "z-index" ) ) {
        continue;
      }
      if ( prop.startsWith( "unicode-bidi" ) ) {
        continue;
      }
      if ( prop.startsWith( "width" ) ) {
        continue;
      }
      if ( prop.startsWith( "min-width" ) ) {
        continue;
      }
      if ( prop.startsWith( "max-width" ) ) {
        continue;
      }
      if ( prop.startsWith( "height" ) ) {
        continue;
      }
      if ( prop.startsWith( "min-height" ) ) {
        continue;
      }
      if ( prop.startsWith( "max-height" ) ) {
        continue;
      }
      if ( prop.startsWith( "vertical-align" ) ) {
        continue;
      }
      if ( prop.startsWith( "overflow" ) ) {
        continue;
      }
      if ( prop.startsWith( "clip" ) ) {
        continue;
      }
      if ( prop.startsWith( "visibility" ) ) {
        continue;
      }
      if ( prop.startsWith( "content" ) ) {
        continue;
      }
      if ( prop.startsWith( "counter-reset" ) ) {
        continue;
      }
      if ( prop.startsWith( "counter-increment" ) ) {
        continue;
      }
      if ( prop.startsWith( "marker-offset" ) ) {
        continue;
      }
      if ( prop.startsWith( "background" ) ) {
        continue;
      }
      if ( prop.startsWith( "text-decoration" ) ) {
        continue;
      }
      if ( prop.startsWith( "text-shadow" ) ) {
        continue;
      }
      if ( prop.startsWith( "table-layout" ) ) {
        continue;
      }
      if ( prop.startsWith( "outline" ) ) {
        continue;
      }

      ALL_INHERITABLE.add( prop );
    }
  }
}

