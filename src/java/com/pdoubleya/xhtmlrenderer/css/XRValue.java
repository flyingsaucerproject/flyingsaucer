/*
 * {{{ header & license
 * XRValue.java
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
package com.pdoubleya.xhtmlrenderer.css;

import java.util.*;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValueList;

/**
 * A CSSValue as parsed from a stylesheet.
 *
 * @author    Patrick Wright
 * @created   August 1, 2004
 */
public interface XRValue extends CSSPrimitiveValue {
  /** Constant for CSS2 value of "important" */
  String IMPORTANT = "important";

  /** Constant for CSS2 value of "inherit" */
  String INHERIT = "inherit";


  /**
   * True if this value specifically marked as inherited. 
   *
   * @return   Returns
   */
  boolean forcedInherit();


  /**
   * True if the value declaration marked as important.
   *
   * @return   The important value
   */
  boolean isImportant();


  /**
   * The value as specified in text.
   *
   * @return   Returns
   */
  String cssText();


  /**
   * A description of the type.
   *
   * @return   Returns
   */
  String cssType();


  /**
   * The short constant ID from dom.css.CSSValue for this assigned value.
   *
   * @return   Returns
   */
  short cssSACPrimitiveValueType();


  /**
   * True if the unit for the specified value is absolute.
   *
   * @return   The absoluteUnit value
   */
  boolean isAbsoluteUnit();


  /**
   * The value as an integer; if conversion fails, returns .MIN_VALUE
   *
   * @return   Returns
   */
  int asInteger();


  /**
   * The value as a float; if conversion fails, returns .MIN_VALUE
   *
   * @return   Returns
   */
  float asFloat();


  /**
   * The value as a double; if conversion fails, returns .MIN_VALUE
   *
   * @return   Returns
   */
  double asDouble();


  /**
   * The value as specified in the CSS
   *
   * @return   Returns
   */
  String asString();

  /**
   * The value as specified in the CSS
   *
   * @return   Returns
   */
  String[] asStringArray();
  
  /**
   * Gets the primitiveType attribute of the XRValue object
   *
   * @return   The primitiveType value
   */
  boolean isPrimitiveType();


  /**
   * Gets the valueList attribute of the XRValue object
   *
   * @return   The valueList value
   */
  boolean isValueList();

}// end interface


