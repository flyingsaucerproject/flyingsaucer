/*
 * {{{ header & license
 * XRDerivedStyleImpl.java
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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.*;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;
import org.w3c.dom.css.RGBColor;

import com.pdoubleya.xhtmlrenderer.css.*;
import com.pdoubleya.xhtmlrenderer.css.constants.*;
import com.pdoubleya.xhtmlrenderer.css.value.BorderColor;

import org.joshy.html.Border;
import org.joshy.html.css.FontResolver;

/**
 * New class
 *
 * @author    Patrick Wright
 * @created   August 2, 2004
 */
public class XRDerivedStyleImpl implements XRDerivedStyle {
  /** Description of the Field */
  private XRElement _xrElement;
  /** Description of the Field */
  private List _matchedStyles;
  /** Description of the Field */
  private Map _derivedPropertiesByName;

  /** The derived border width for this RuleSet */
  private Border _drvBorderWidth;

  /** The derived margin width for this RuleSet */
  private Border _drvMarginWidth;

  /** The derived padding width for this RuleSet */
  private Border _drvPaddingWidth;

  /** The derived background color value for this RuleSet */
  private Color _drvBackgroundColor;

  /** The derived border color value for this RuleSet */
  private BorderColor _drvBorderColor;

  /** The derived Color value for this RuleSet */
  private Color _drvColor;

  /** The derived Font value for this RuleSet */
  private Font _drvFont;


  /**
   * Constructor for the XRDerivedStyleImpl object
   *
   * @param forElement  PARAM
   * @param iter        PARAM
   */
  public XRDerivedStyleImpl( XRElement forElement, Iterator iter ) {
    this();
    _xrElement = forElement;

    while ( iter.hasNext() ) {
      _matchedStyles.add( iter.next() );
    }
    derive();
  }


  /**
   * Constructor for the XRDerivedStyleImpl object
   *
   * @param forElement  PARAM
   * @param rule        PARAM
   */
  public XRDerivedStyleImpl( XRElement forElement, XRStyleRule rule ) {
    _xrElement = forElement;

    _matchedStyles.add( rule );

    List props = pullFromRule( rule );
    Iterator iter = props.iterator();
    while ( iter.hasNext() ) {
      XRProperty property = (XRProperty)iter.next();
      _derivedPropertiesByName.put( property.propertyName(), property );
    }
  }


  /** Constructor for the XRDerivedStyleImpl object */
  private XRDerivedStyleImpl() {
    _matchedStyles = new ArrayList();
    _derivedPropertiesByName = new TreeMap();
  }


  /** Description of the Method */
  public void dump() {
    System.out.println( "Derivation complete: " + _derivedPropertiesByName );
  }


  /**
   * Description of the Method
   *
   * @return   Returns
   */
  public Iterator listXRProperties() {
    return _derivedPropertiesByName.values().iterator();
  }

  /** 
   * Merges two XRRuleSets, combining all properties. This is not used for cascading, rather for
   * two rules defined separately in the same sheet with the same selector. Any properties with the
   * same name in fromRuleSet will replace existing properties with that name in this XRRuleSet.
   */
   public synchronized void mergeProperties(XRRuleSet fromRuleSet) {
      Iterator iter = fromRuleSet.listXRProperties();
      while ( iter.hasNext()) {
        XRProperty prop = (XRProperty)iter.next(); 
        _derivedPropertiesByName.put(prop.propertyName(), prop);
      }
   }

  /**
   * Returns a XRProperty by name. Because we are a derived style, the property
   * will already be resolved at this point--the method is synchronized in order
   * to allow this resolution to happen safely. Thus, on this XRProperty you can
   * call computedValue() or actualValue() to get something meaningful.
   *
   * @param propName  PARAM
   * @return          Returns
   */
  public synchronized XRProperty propertyByName( String propName ) {
    // HERE: when we get the property, check if it is resolved
    // if not, call resolve() and pass it our parent's reference
    // or just our XRElement
    XRProperty prop = (XRProperty)_derivedPropertiesByName.get( propName );
    prop.resolveValue( _xrElement );
    return prop;
  }


  /**
   * Gets the propertyCssText attribute of the XRDerivedStyle object
   *
   * @param propName  PARAM
   * @return          The propertyCssText value
   */
  public String getPropertyCssText( String propName ) {
    return propertyByName( propName ).cssText();
  }


  /**
   * Convenience property accessor; returns a Border initialized with the
   * four-sided border width. Uses the actual value (computed actual value) for
   * this element.
   *
   * @return   The borderWidth value
   */
  public synchronized Border getBorderWidth() {
    if ( _drvBorderWidth == null ) {
      Border border = new Border();
      // ASK: why is Josh forcing to an int in CSSAccessor? don't we want float/pixels?
      border.top = (int)propertyByName( CSSName.BORDER_WIDTH_TOP ).actualValue().asFloat();
      border.bottom = (int)propertyByName( CSSName.BORDER_WIDTH_BOTTOM ).actualValue().asFloat();
      border.left = (int)propertyByName( CSSName.BORDER_WIDTH_LEFT ).actualValue().asFloat();
      border.right = (int)propertyByName( CSSName.BORDER_WIDTH_RIGHT ).actualValue().asFloat();
      _drvBorderWidth = border;
    }
    return _drvBorderWidth;
  }


  /**
   * Convenience property accessor; returns a Border initialized with the
   * four-sided margin width. Uses the actual value (computed actual value) for
   * this element.
   *
   * @return   The marginWidth value
   */
  public synchronized Border getMarginWidth() {
    if ( _drvMarginWidth == null ) {
      Border border = new Border();
      // ASK: why is Josh forcing to an int in CSSAccessor? don't we want float/pixels?
      border.top = (int)propertyByName( CSSName.MARGIN_TOP ).actualValue().asFloat();
      border.bottom = (int)propertyByName( CSSName.MARGIN_BOTTOM ).actualValue().asFloat();
      border.left = (int)propertyByName( CSSName.MARGIN_LEFT ).actualValue().asFloat();
      border.right = (int)propertyByName( CSSName.MARGIN_RIGHT ).actualValue().asFloat();
      _drvMarginWidth = border;
    }
    return _drvMarginWidth;
  }


  /**
   * Convenience property accessor; returns a Border initialized with the
   * four-sided padding width. Uses the actual value (computed actual value) for
   * this element.
   *
   * @return   The paddingWidth value
   */
  public synchronized Border getPaddingWidth() {
    if ( _drvPaddingWidth == null ) {
      Border border = new Border();
      // ASK: why is Josh forcing to an int in CSSAccessor? don't we want float/pixels?
      border.top = (int)propertyByName( CSSName.PADDING_TOP ).actualValue().asFloat();
      border.bottom = (int)propertyByName( CSSName.PADDING_BOTTOM ).actualValue().asFloat();
      border.left = (int)propertyByName( CSSName.PADDING_LEFT ).actualValue().asFloat();
      border.right = (int)propertyByName( CSSName.PADDING_RIGHT ).actualValue().asFloat();
      _drvPaddingWidth = border;
    }
    return _drvPaddingWidth;
  }


  /**
   * Convenience property accessor; returns a Color initialized with the
   * background color value; Uses the actual value (computed actual value) for
   * this element.
   *
   * @return   The backgroundColor value
   */
  public synchronized Color getBackgroundColor() {
    if ( _drvBackgroundColor == null ) {
      _drvBackgroundColor = rgbToColor( propertyByName( CSSName.BACKGROUND_COLOR ).actualValue().getRGBColorValue() );
    }
    return _drvBackgroundColor;
  }


  /**
   * Convenience property accessor; returns a BorderColor initialized with the
   * four-sided border color. Uses the actual value (computed actual value) for
   * this element.
   *
   * @return   The borderColor value
   */
  public synchronized BorderColor getBorderColor() {
    if ( _drvBorderColor == null ) {
      BorderColor bcolor = new BorderColor();
      bcolor.topColor = rgbToColor( propertyByName( CSSName.BORDER_COLOR_TOP ).actualValue().getRGBColorValue() );
      bcolor.rightColor = rgbToColor( propertyByName( CSSName.BORDER_COLOR_RIGHT ).actualValue().getRGBColorValue() );
      bcolor.bottomColor = rgbToColor( propertyByName( CSSName.BORDER_COLOR_BOTTOM ).actualValue().getRGBColorValue() );
      bcolor.leftColor = rgbToColor( propertyByName( CSSName.BORDER_COLOR_LEFT ).actualValue().getRGBColorValue() );
      _drvBorderColor = bcolor;
    }
    return _drvBorderColor;
  }


  /**
   * Convenience property accessor; returns a Color initialized with the
   * foreground color Uses the actual value (computed actual value) for this
   * element.
   *
   * @return   The color value
   */
  public synchronized Color getColor() {
    if ( _drvColor == null ) {
      _drvColor = rgbToColor( propertyByName( CSSName.COLOR ).actualValue().getRGBColorValue() );
    }
    return _drvColor;
  }


  /**
   * Convenience property accessor; returns the Font to use for text on this
   * element. Uses the actual value (computed actual value) for this element.
   *
   * @return   The font value
   */
  public synchronized Font getFont(Graphics g) {
    // TODO: this is based on Josh's original implementation...FontResolver though may need some work
    if ( _drvFont == null ) {
      String families[] = propertyByName(CSSName.FONT_FAMILY).actualValue().asStringArray();
      float size  = propertyByName(CSSName.FONT_SIZE).actualValue().asFloat();
      String weight = propertyByName(CSSName.FONT_WEIGHT).actualValue().asString();
      String style = propertyByName(CSSName.FONT_STYLE).actualValue().asString();
      _drvFont = new FontResolver().resolveFont(g.getFont(), families, size, weight, style); 
    }
    return _drvFont;
  }


  /**
   * Copied from Josh M.'s CSSAccessor class
   *
   * @param rgbcol  PARAM
   * @return        Returns
   */
  private Color rgbToColor( RGBColor rgbcol ) {

    return new java.awt.Color( rgbcol.getRed().getFloatValue( CSSPrimitiveValue.CSS_NUMBER ) / 255f,
        rgbcol.getGreen().getFloatValue( CSSPrimitiveValue.CSS_NUMBER ) / 255f,
        rgbcol.getBlue().getFloatValue( CSSPrimitiveValue.CSS_NUMBER ) / 255f );
  }


  /**
   * <p>
   *
   * Implements cascade/inherit/important logic. This should result in the
   * element for this style having a value for *each and every* (visual)
   * property in the CSS2 spec. The implementation is based on the notion that
   * the matched styles are given to us in a perfectly sorted order, such that
   * properties appearing later in the rule-set always override properties
   * appearing earlier. It also assumes that all properties in the CSS2 spec are
   * defined somewhere across all the matched styles; for example, that the
   * full-property set is given in the user-agent CSS that is always loaded with
   * styles. The current implementation makes no attempt to check either of
   * these assumptions. When this method exits, the derived property list for
   * this class will be populated with the properties defined for this element,
   * properly cascaded.</p>
   */
  private void derive() {
    Iterator mStyles = _matchedStyles.iterator();
    while ( mStyles.hasNext() ) {
      XRStyleRule rule = (XRStyleRule)mStyles.next();
      // IDEA: at this point could run a compareTo() check on this rule and the prev in the loop, sanity check
      Iterator props = rule.listXRProperties();

      while ( props.hasNext() ) {
        XRProperty prop = (XRProperty)props.next();
        System.out.println("derive: adding property--" + prop);
        _derivedPropertiesByName.put( prop.propertyName(), prop );
      }
    }
    System.out.println("done with derive");
  }


  /**
   * Implements cascade/inherit/important logic. This should result in the
   * element for this style having a value for *each and every* (visual)
   * property in the CSS2 spec.
   */
  // This was the original version written by Patrick Wright
  // replaced in favor of sort-based version in approach suggested by Scott
  private void deriveOld() {
    // loop all known (visual) properties, checking for a value
    // if we don't find it in our matched styles, we look to parent
    //
    // OPTM: if we can run this from root to leaf, could improve performance
    // by making resolved parent properties directly available...
    Iterator propNames = CSSName.allCSS2PropertyNames();
    while ( propNames.hasNext() ) {
      String propName = (String)propNames.next();

      // is property defined in a matched style?
      Iterator mStyles = _matchedStyles.iterator();

      // these "the" properties denote the current Property for our property name, with its seq, spec, sheet seq
      XRProperty theProp = null;
      int theSheetSeq = 0;
      int theSpec = 0;
      int theSeq = 0;

      while ( mStyles.hasNext() ) {
        XRStyleRule rule = (XRStyleRule)mStyles.next();

        XRProperty prop = rule.propertyByName( propName );
        if ( prop != null ) {
          int sheetSeq = rule.getStyleSheet().sequence();
          int spec = rule.selectorSpecificity();
          int seq = rule.sequenceInStyleSheet();

          // if we don't have any Property for this property name or
          // if we have one, but the one we just found has higher
          // precedence in stylesheet, spec, or seq, use it
          if ( theProp == null ||
              ( theProp != null &&
              ( sheetSeq > theSheetSeq || ( ( spec > theSpec ) || ( spec == theSpec && seq > theSeq ) ) ) ) ) {
            theProp = prop;
            theSheetSeq = sheetSeq;
            theSpec = spec;
            theSeq = seq;
          }
        }
      }// loop styles

      // could not find property in our styles
      // or as a property it inherits
      // or found it, but specified value "inherit"
      if ( ( theProp == null && CSSName.propertyInherits( propName ) ) ||
          ( theProp != null && theProp.specifiedValue().forcedInherit() ) ) {

        // check our parent. this may be recursive to root in worst case
        // but should not generally be, as all our elements should have
        // default matches from default.css, which would have some initial
        // value, not value "inherit"
        XRElement parent = _xrElement.parentXRElement();
        if ( parent != null ) {
          XRProperty prop = parent.derivedStyle().propertyByName( propName );
          if ( prop != null ) {
            if ( theProp == null ||
                ( theProp != null &&
                ( prop.specifiedValue().isImportant() && !theProp.specifiedValue().isImportant() ) ||
                ( theProp.specifiedValue().forcedInherit() ) ) ) {
              theProp = prop;
            }
          }
        }
      }

      _derivedPropertiesByName.put( propName, theProp );
    }
  }


  /**
   * just extracts all XRProperties from a list of StyleRules into a List
   *
   * @param styles  PARAM
   * @return        Returns
   */
  private List pullProperties( List styles ) {
    List derA = new ArrayList();
    Iterator iter = styles.iterator();
    while ( iter.hasNext() ) {
      XRStyleRule rule = (XRStyleRule)iter.next();
      derA.add( pullFromRule( rule ) );
    }
    return derA;
  }


  /**
   * just extracts all XRProperties from a rule into a List
   *
   * @param rule  PARAM
   * @return      Returns
   */
  private List pullFromRule( XRStyleRule rule ) {
    List der = new ArrayList();
    Iterator props = rule.listXRProperties();
    while ( props.hasNext() ) {
      der.add( props.next() );
    }
    return der;
  }
}

