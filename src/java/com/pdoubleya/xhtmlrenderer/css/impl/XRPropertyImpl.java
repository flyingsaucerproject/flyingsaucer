/*
 * {{{ header & license
 * XRPropertyImpl.java
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
package com.pdoubleya.xhtmlrenderer.css.impl;

import java.util.*;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

import com.pdoubleya.xhtmlrenderer.css.*;
import com.pdoubleya.xhtmlrenderer.css.constants.*;
import com.pdoubleya.xhtmlrenderer.css.factory.*;

/**
 * Default XRProperty implementation.
 *
 * @author    Patrick Wright
 * @created   August 1, 2004
 */
public class XRPropertyImpl implements XRProperty {
  /**
   * The property name to PropertyFactory mappings. The factories facilitate
   * creating XRProperties from a property definition. Note static block is at
   * end of class.
   */
  private final static Map PRP_FACTORIES;

  /** The Style where this property originated. */
  private CSSStyleDeclaration _style;

  /** Property's text name, e.g. "margin-top" */
  private String _propName;

  /** Sequence in which this property was found in declaration, 0-based. */
  private int _sequence;

  /** The value as specified in the declaration */
  private XRValue _specifiedValue;

  /** Relative value as computed for the current context */
  private XRValue _computedValue;

  /** Computed value as restricted for the current environment */
  private XRValue _actualValue;

  /**
   * Whether the property has been resolved relative to the current environment
   * or not
   */
  private boolean _isResolved;

  // seq = 0;
  /**
   * Constructor for the XRPropertyImpl object
   *
   * @param style     PARAM
   * @param propName  PARAM
   */
  public XRPropertyImpl( CSSStyleDeclaration style, String propName ) {
    this( style, propName, 0 );
  }


  /**
   * Constructor for the XRPropertyImpl object
   *
   * @param style     PARAM
   * @param propName  PARAM
   * @param sequence  PARAM
   */
  public XRPropertyImpl( CSSStyleDeclaration style, String propName, int sequence ) {
    this( style, propName, sequence, new XRValueImpl( (CSSPrimitiveValue)style.getPropertyCSSValue( propName ),
        style.getPropertyPriority( propName ) ) );
  }


  /**
   * Constructor for the XRPropertyImpl object
   *
   * @param style     PARAM
   * @param propName  PARAM
   * @param sequence  PARAM
   * @param value     PARAM
   */
  public XRPropertyImpl( CSSStyleDeclaration style, String propName, int sequence, XRValue value ) {
    _style = style;
    _propName = propName;
    _sequence = sequence;
    _specifiedValue = value;
  }


  /**
   * Utility method to instantiate a group of XRProperties out of a style
   * rule/declaration. Returns an iterator of the instantiated properties--the
   * are not attached to the declaration or otherwise stored.
   *
   * @param style     PARAM
   * @param propName  PARAM
   * @param sequence  PARAM
   * @return          Returns
   */
  public static Iterator fromCSSPropertyDecl( CSSStyleDeclaration style, String propName, int sequence ) {
    List list = new ArrayList();
    PropertyFactory factory = (PropertyFactory)PRP_FACTORIES.get( propName );
    switch ( style.getPropertyCSSValue( propName ).getCssValueType() ) {
        case CSSValue.CSS_PRIMITIVE_VALUE:
          if ( factory == null ) {
            XRProperty prop = new XRPropertyImpl( style, propName, sequence );
            list.add( prop );
          } else {
            Iterator iter = factory.explodeProperties( style, propName, sequence );
            while ( iter.hasNext() ) {
              list.add( iter.next() );
            }
          }
          break;
        case CSSValue.CSS_VALUE_LIST:
          if ( factory == null ) {
            System.err.println( "! Property '" + propName + "' was assigned multiple values but only single value is allowed." );
          } else {
            Iterator iter = factory.explodeProperties( style, propName, sequence );
            while ( iter.hasNext() ) {
              list.add( iter.next() );
            }
          }
          break;
        default:
          System.err.println( "Property '" + propName + "' is neither primitive nor value list, can't handle." );
          break;
    }

    return list.iterator();
  }


  /**
   * Returns true if this property type ("font-family", "margin-top") inherits
   * its value from the parent element of the element to which it applies. Thus,
   * if this were an instance of a "margin-top" property, would return false; if
   * it were a "font-family", would return true.
   *
   * @return   Returns
   */
  public boolean propertyValueInherits() {
    return CSSName.propertyInherits( _propName );
  }


  /**
   * The numeric sequence in which this property was found in the
   * declaration/rule
   *
   * @return   Returns
   */
  public int sequenceInRule() {
    return _sequence;
  }


  /**
   * The computed value, with any modifications forced by the presentation
   * environment e.g. limitation in color range.
   *
   * @return   Returns
   */
  public XRValue actualValue() {
    return ( _specifiedValue.isAbsoluteUnit() ? _specifiedValue : _actualValue );
  }


  /**
   * The value as specified in the sheet, and computed for use (if not absolute)
   *
   * @return   Returns
   */
  public XRValue computedValue() {
    return ( _specifiedValue.isAbsoluteUnit() ? _specifiedValue : _computedValue );
  }


  /**
   * The text defining this property's value as specified. 
   *
   * @return   Returns
   */
   // ASK: should this be CSS2-style, e.g. prop-name: value;
  public String cssText() {
    return _specifiedValue.getCssText();// IMPL
  }


  /**
   * The value for this property if none other is specified.
   *
   * @return   Returns
   */
  public XRValue initialValue() {
    // TODO: eventually, should have initial values for all properties
    return _specifiedValue;
  }


  /**
   * The value as specified in the sheet.
   *
   * @return   Returns
   */
  public XRValue specifiedValue() {
    return _specifiedValue;
  }


  /**
   * The plain-text property name, should be CSS2 valid.
   *
   * @return   Returns
   */
  public String propertyName() {
    return _propName;
  }


  /**
   * Description of the Method
   *
   * @return   Returns
   */
  public String toString() {
    return _propName + "=" + _specifiedValue;
  }


  /**
   * If the property has a relative value that has not been computed, computes
   * it. After the property value is resolved, computedValue() and actualValue()
   * will return meaningful results.
   *
   * @param elemContext  PARAM
   */
  public synchronized void resolveValue( XRElement elemContext ) {
    if ( isResolved() ) {
      return;
    }

    XRValue computed = _specifiedValue;
    if ( _specifiedValue.forcedInherit() ) {
      XRElement parent = elemContext.parentXRElement();
      if ( parent == null ) {
        System.err.println( "XRPropertyImpl: trying to resolve an inherited property, but have no parent XRElement (root of document?)--property '" + propertyName() + "' may not be defined in CSS." );
        // ASK: probably a good place to shove initial values?
      } else {
        // this is indirectly recursive because propertyByName() will end up here again
        // but in parent element context
        computed = parent.derivedStyle().propertyByName( propertyName() ).actualValue();
      }
    }

    _computedValue = computed;
    _actualValue = computed;

    _isResolved = true;
  }


  /**
   * Returns true if the specified value for the property was explicitly set to
   * inherit.
   *
   * @return   The inherited value
   */
  public boolean isInherited() {
    return _specifiedValue.forcedInherit();
  }


  /**
   * Returns true if the property has an absolute value, or a relative value
   * that has been correctly computed.
   *
   * @return   The resolved value
   */
  public boolean isResolved() {
    return _specifiedValue.isAbsoluteUnit() || _isResolved;
  }

  static {
    PRP_FACTORIES = new HashMap();

    PRP_FACTORIES.put( CSSName.MARGIN_SHORTHAND, MarginPropertyFactory.instance() );

    PRP_FACTORIES.put( CSSName.PADDING_SHORTHAND, PaddingPropertyFactory.instance() );

    PRP_FACTORIES.put( CSSName.BORDER_SHORTHAND, BorderPropertyFactory.instance() );

    PRP_FACTORIES.put( CSSName.BORDER_WIDTH_SHORTHAND, BorderWidthPropertyFactory.instance() );
    PRP_FACTORIES.put( CSSName.BORDER_COLOR_SHORTHAND, BorderColorPropertyFactory.instance() );
    PRP_FACTORIES.put( CSSName.BORDER_STYLE_SHORTHAND, BorderStylePropertyFactory.instance() );

    PRP_FACTORIES.put( CSSName.BORDER_TOP_SHORTHAND, BorderSidePropertyFactory.instance() );
    PRP_FACTORIES.put( CSSName.BORDER_RIGHT_SHORTHAND, BorderSidePropertyFactory.instance() );
    PRP_FACTORIES.put( CSSName.BORDER_BOTTOM_SHORTHAND, BorderSidePropertyFactory.instance() );
    PRP_FACTORIES.put( CSSName.BORDER_LEFT_SHORTHAND, BorderSidePropertyFactory.instance() );
  }
}// end class

