/*
 * {{{ header & license
 * XRProperty.java
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
import org.w3c.dom.css.CSSStyleDeclaration;

/**
 * A property read from a CSS2 style declaration. The property gives you access
 * to the value (initial, specified, computed, actual).
 *
 * @author    Patrick Wright
 * @created   July 30, 2004 
 */
// ASK: should Properties have a reference back to their rules?
// ASK: should Properties have a reference back to their stylesheets? 
public interface XRProperty {

  /**
   * The name of this property--as supplied in stylesheet, should also be a
   * valid property name in CSSName
   *
   * @return   Returns
   */
  String propertyName();


  /**
   * Returns true if this property type ("font-family", "margin-top") inherits
   * its value from the parent element of the element to which it applies. Thus,
   * if this were an instance of a "margin-top" property, would return false; if
   * it were a "font-family", would return true.
   *
   * @return   Returns
   */
  boolean propertyValueInherits();


  /**
   * Returns true if this specific property has had its value inherited. To
   * check if the value for this property was set to "inherit", you have to
   * check the XRValue itself.
   *
   * @return   The inherited value
   */
  boolean isInherited();


  /**
   * The initial value of this property--this is the same regardless of
   * sheets--it is part of specification.
   *
   * @return   Returns
   */
  XRValue initialValue();


  /**
   * The value as specified by stylesheet (CSS2 6.1.1)
   *
   * @return   Returns
   */
  XRValue specifiedValue();


  /**
   * The value as computed--if specified was relative, this value will be
   * absolute; otherwise, the same as the specified value (CSS2 6.1.2)
   *
   * @return   Returns
   */
  XRValue computedValue();


  /**
   * The actual value--the computed value when limited by the current
   * presentation environment. (CSS2 6.1.3)
   *
   * @return   Returns
   */
  XRValue actualValue();


  /**
   * Returns the text for the property.
   *
   * @return   Returns
   */
  String cssText();


  /**
   * The numeric sequence in which this property was found in the
   * declaration/rule
   *
   * @return   Returns
   */
  int sequenceInRule();


  /**
   * Returns true if the property has an absolute value, or a relative value
   * that has been correctly computed.
   *
   * @return   The resolved value
   */
  boolean isResolved();


  /**
   * If the property has a relative value that has not been computed, computes it.
   * After the property value is resolved, computedValue() and actualValue() will
   * return meaningful results. Values are resolved relative to the current XRElement
   * context (to which this property belongs), or to the parent XRElement context.
   *
   * @return   Returns
   */
  void resolveValue(XRElement elemContext);
}// end interface


