/*
 * {{{ header & license
 * XRValueImpl.java
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

import java.lang.reflect.*;
import java.util.*;
import org.w3c.dom.DOMException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.RGBColor;
import org.w3c.dom.css.Rect;
import org.w3c.dom.css.Counter;
import org.w3c.dom.css.CSSValueList;

import org.joshy.html.box.Box;

import com.pdoubleya.xhtmlrenderer.css.*;

/**
 * A primitive value assigned to an XRProperty. XRValue allows for easy type conversions, relative
 * value derivation, etc. The class is intended to "wrap" a CSSValue from a SAC CSS parser.
 *
 * Note that not all type conversions make sense, and that some won't make sense until 
 * relative values are resolved. You should check with the cssSACPrimitiveValueType() to see
 * if the value conversion you are requesting is rational.
 *
 * STATUS: this class is currently mostly unfinished--there is lots to be done for type
 * conversion, computed value derivation, etc. Currently proof of concept for the class design.
 *
 * @author    Patrick Wright
 * @created   August 1, 2004
 */
public class XRValueImpl implements XRValue {
  // ASK: need to clarify if this class is for both List and Primitives, or just primitives...

  /** The DOM CSSPrimitiveValue we are given from the Parse */
  private CSSPrimitiveValue _domCSSPrimitiveValue;

  /** The value as text */
  private String _domValueText;

  /** The priority, either "" or "important" */
  private String _domPriority;
  
  /** Type descriptions--a crude approximation taken by scanning CSSPrimitiveValue statics */
  private static final List TYPE_DESCRIPTIONS;  


  /**
   * Constructor for the XRValueImpl object
   *
   * @param domCSSValue  PARAM
   * @param domPriority  PARAM
   */
  public XRValueImpl( CSSPrimitiveValue domCSSValue, String domPriority ) {
    _domCSSPrimitiveValue = domCSSValue;
    _domValueText = domCSSValue.getCssText();
    _domPriority = domPriority;
  }


  /**
   * The value as a double; returns Double.MIN_VALUE (as double) if there is an error.
   *
   * @return   Returns
   */
  public double asDouble() {
    double d = new Double(Double.MIN_VALUE).doubleValue();
    try {
      d = Double.valueOf(_domValueText).doubleValue();    
     } catch (Exception ex) {
       System.err.println("Value '" + _domValueText + "' is not a valid double." );
     } 
    return d;
  }


  /**
   * The value as a float; returns Float.MIN_VALUE (as float) if there is an error.
   *
   * @return   Returns
   */
  public float asFloat() {
    float f = new Float(Float.MIN_VALUE).floatValue();
    try {
      f = Float.valueOf(_domValueText).floatValue();    
     } catch (Exception ex) {
       System.err.println("Value '" + _domValueText + "' is not a valid float." );
     } 
    return f;
  }


  /**
   * The value as a double; returns Integer.MIN_VALUE (as int) if there is an error.
   *
   * @return   Returns
   */
  public int asInteger() {
    int i = new Integer(Integer.MIN_VALUE).intValue();
    try {
      i = Integer.valueOf(_domValueText).intValue();    
     } catch (Exception ex) {
       System.err.println("Value '" + _domValueText + "' is not a valid integer." );
     } 
    return i;
  }

  /**
   * value as a string...same as cssText() but kept for parallel with other as<type>... methods
   *
   * @return   Returns
   */
  public String asString() {
    return cssText();
  }

  /**
   * value as a string...same as cssText() but kept for parallel with other as<type>... methods
   *
   * @return   Returns
   */
  public String[] asStringArray() {
    return cssText().split(",");
  }

/* CLEAN
   public Iterator cssValues() {
    List list = new ArrayList();
    CSSValueList vlist = (CSSValueList)_domCSSPrimitiveValue;
    for ( int i = 0, len = vlist.getLength(); i < len; i++ ) {
      list.add( vlist.item( i ) );
    }
    return list.iterator();
  } */


  // the CSSPrimitiveValue type if we are wrapping a CSSPrimitiveValue, type
  // CSSPrimitiveValue.CSS_UNKNOWN if we are not wrapping a primitive; best to
  // check if we are wrapping a primitive first
  /**
   * Description of the Method
   *
   * @return   Returns
   */
  public short cssSACPrimitiveValueType() {
    return _domCSSPrimitiveValue.getPrimitiveType();
  }

  /**
   * Description of the Method
   *
   * @return   Returns
   */
  public short getPrimitiveType() {
    return _domCSSPrimitiveValue.getPrimitiveType();
  }  

  /**
   * Description of the Method
   *
   * @return   Returns
   */
  public String cssText() {
    return _domCSSPrimitiveValue.getCssText();
  }
  
  /**
   * Description of the Method
   *
   * @return   Returns
   */
  public String getStringValue() {
    return _domCSSPrimitiveValue.getStringValue();
  }

  /**
   * Description of the Method
   *
   * @return   Returns
   */
  public void setStringValue(short index, String s) {
    _domCSSPrimitiveValue.setStringValue(index, s);
  }

  /**
   * Description of the Method
   *
   * @return   Returns
   */
  public float getFloatValue(short unitType) {
    return _domCSSPrimitiveValue.getFloatValue(unitType);
  }

  /**
   * Description of the Method
   *
   * @return   Returns
   */
  public void setFloatValue(short unitType, float val) {
    _domCSSPrimitiveValue.setFloatValue(unitType, val);
  }

  /**
   * Description of the Method
   *
   * @return   Returns
   */
  public Counter getCounterValue() {
    return _domCSSPrimitiveValue.getCounterValue();
  }

  /**
   * Description of the Method
   *
   * @return   Returns
   */
  public RGBColor getRGBColorValue() {
    return _domCSSPrimitiveValue.getRGBColorValue();
  }

  /**
   * Description of the Method
   *
   * @return   Returns
   */
  public Rect getRectValue() {
    return _domCSSPrimitiveValue.getRectValue();
  }

  /**
   * Description of the Method
   *
   * @return   Returns
   */
  public boolean forcedInherit() {
    return _domCSSPrimitiveValue.getCssText().indexOf( "inherit" ) >=0;
  }


  /**
   * A text representation of the value, for dumping
   *
   * @return   Returns
   */
  public String toString() {
    return getCssText() + " (" + cssType() + "--" + getCssValueTypeDesc() + ")\n" +
        "   " + ( isImportant() ? "" : "not " ) + "important" + "\n" +
        "   " + ( forcedInherit() ? "" : "not " ) + "inherited";
  }


  /**
   * Sets the cssText attribute of the XRValueImpl object
   *
   * @param str               The new cssText value
   * @exception DOMException  Throws
   */
  public void setCssText( String str )
    throws DOMException {
    _domCSSPrimitiveValue.setCssText( str );
  }


  /**
   * Returns true if the specified value was absolute (even if we have a computed
   * value for it)
   *
   * @return   The absoluteUnit value
   */
  public boolean isAbsoluteUnit() {
    // TODO: check this list...
    // WARN: this will fail if not a primitive value
    switch ( ( (CSSPrimitiveValue)_domCSSPrimitiveValue ).getPrimitiveType() ) {
        // relative length or size
        case CSSPrimitiveValue.CSS_EMS:
        case CSSPrimitiveValue.CSS_EXS:
        case CSSPrimitiveValue.CSS_PX:
        case CSSPrimitiveValue.CSS_PERCENTAGE:
          return false;
          
        // length
        case CSSPrimitiveValue.CSS_IN:
        case CSSPrimitiveValue.CSS_CM:
        case CSSPrimitiveValue.CSS_MM:
        case CSSPrimitiveValue.CSS_PT:
        case CSSPrimitiveValue.CSS_PC:

        // color
        case CSSPrimitiveValue.CSS_RGBCOLOR:

        // ?
        case CSSPrimitiveValue.CSS_ATTR:
        case CSSPrimitiveValue.CSS_DIMENSION:
        case CSSPrimitiveValue.CSS_IDENT:
        case CSSPrimitiveValue.CSS_NUMBER:
        case CSSPrimitiveValue.CSS_RECT:

        // counters
        case CSSPrimitiveValue.CSS_COUNTER:

        // angles
        case CSSPrimitiveValue.CSS_DEG:
        case CSSPrimitiveValue.CSS_GRAD:
        case CSSPrimitiveValue.CSS_RAD:

        // aural - freq
        case CSSPrimitiveValue.CSS_HZ:
        case CSSPrimitiveValue.CSS_KHZ:

        // time
        case CSSPrimitiveValue.CSS_S:
        case CSSPrimitiveValue.CSS_MS:

        // URI
        case CSSPrimitiveValue.CSS_URI:

        case CSSPrimitiveValue.CSS_STRING:
          return true;
          
        case CSSPrimitiveValue.CSS_UNKNOWN:
        default:
          return false;
    }
  }


  /**
   * Gets the primitiveType attribute of the XRValueImpl object
   *
   * @return   The primitiveType value
   */
  public boolean isPrimitiveType() {
    return getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE;
  }


  /**
   * Gets the valueList attribute of the XRValueImpl object
   *
   * @return   The valueList value
   */
  public boolean isValueList() {
    return getCssValueType() == CSSValue.CSS_VALUE_LIST;
  }


  /**
   * Gets the cssText attribute of the XRValueImpl object
   *
   * @return   The cssText value
   */
  public String getCssText() {
    return _domCSSPrimitiveValue.getCssText();
  }


  /**
   * Gets the cssValueType attribute of the XRValueImpl object
   *
   * @return   The cssValueType value
   */
  public short getCssValueType() {
    return _domCSSPrimitiveValue.getCssValueType();
  }


  /**
   * Gets the cssValueTypeDesc attribute of the XRValueImpl object
   *
   * @return   The cssValueTypeDesc value
   */
  public String getCssValueTypeDesc() {
    switch ( getCssValueType() ) {
        case CSS_CUSTOM:
          return "CSS_CUSTOM";
        case CSS_INHERIT:
          return "CSS_INHERIT";
        case CSS_PRIMITIVE_VALUE:
          return "CSS_PRIMITIVE_VALUE";
        case CSS_VALUE_LIST:
          return "CSS_VALUE_LIST";
        default:
          return "UNKNOWN";
    }
  }


  /**
   * Gets the important attribute of the XRValueImpl object
   *
   * @return   The important value
   */
  public boolean isImportant() {
    return _domPriority != null && _domPriority.equals( IMPORTANT );
  }

  /**
   * A text representation of the CSS type for this value.
   *
   * @return   Returns
   */
  public String cssType() {
    String desc = (String)TYPE_DESCRIPTIONS.get(( (CSSPrimitiveValue)_domCSSPrimitiveValue ).getPrimitiveType());
    if ( desc == null ) desc = "{UNKNOWN VALUE TYPE}";
    return desc;
  }
  
  public static void strings() {
    
  }

  static {
    SortedMap map = new TreeMap();
    TYPE_DESCRIPTIONS = new ArrayList();
    try {
      Field fields[] = CSSPrimitiveValue.class.getFields();
      for ( int i=0; i < fields.length; i++ ) {
        Field f = fields[i];
        int mod = f.getModifiers();
        if ( Modifier.isFinal(mod) && 
             Modifier.isStatic(mod) && 
             Modifier.isPublic(mod) ) {
               
          Short val = (Short)f.get(null);
          String name = f.getName();
          if ( name.startsWith("CSS_"))
            if ( !name.equals("CSS_INHERIT") &&
                 !name.equals("CSS_PRIMITIVE_VALUE") &&
                 !name.equals("CSS_VALUE_LIST") &&
                 !name.equals("CSS_CUSTOM")) 
                 
                 map.put(val, name.substring("CSS_".length())); 
        }        
      }
      Iterator iter = map.values().iterator();
      while ( iter.hasNext()) {
        TYPE_DESCRIPTIONS.add(iter.next()); 
      }
    } catch ( Exception ex ) {
      ex.printStackTrace();
    }    
  }
} // end class

