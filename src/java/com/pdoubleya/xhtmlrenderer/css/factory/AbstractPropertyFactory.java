/*
 * {{{ header & license
 * AbstractPropertyFactory.java
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
package com.pdoubleya.xhtmlrenderer.css.factory;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSStyleDeclaration;

import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

import com.pdoubleya.xhtmlrenderer.css.*;
import com.pdoubleya.xhtmlrenderer.css.impl.*;

// package PACKAGE;

/**
 * New class
 *
 * @author    Patrick Wright
 * @created   August 2, 2004
 */
public abstract class AbstractPropertyFactory implements PropertyFactory {
  /**
   * Description of the Method
   *
   * @param newPropertyName  PARAM
   * @param primitive        PARAM
   * @param priority         PARAM
   * @param style            PARAM
   * @param sequence         PARAM
   * @return                 Returns
   */
  protected XRPropertyImpl newProperty(
      String newPropertyName,
      CSSPrimitiveValue primitive,
      String priority,
      CSSStyleDeclaration style,
      int sequence ) {

    XRValue val = new XRValueImpl( primitive, priority );
    return new XRPropertyImpl( style, newPropertyName, sequence, val );
  }
}

