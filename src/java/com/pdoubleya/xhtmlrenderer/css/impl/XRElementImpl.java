/*
 * {{{ header & license
 * XRElementImpl.java
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
/*
 * :tabSize=4:indentSize=4:noTabs=true:
 * :folding=explicit:collapseFolds=1:
 */
package com.pdoubleya.xhtmlrenderer.css.impl;

import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.pdoubleya.xhtmlrenderer.css.*;

import org.joshy.html.box.Box;

/**
 * Implementation of XRElement, see interface for comments.
 *
 * @author    Patrick Wright
 * @created   August 2, 2004
 */
public class XRElementImpl implements XRElement {
  /** Description of the Field */
  private Document _document;
  /** Description of the Field */
  private Element _element;
  /** Description of the Field */
  private XRElement _xrParent;
  /** Description of the Field */
  private List _matchedStyles;
  /** Description of the Field */
  private XRDerivedStyle _derivedStyle;


  /**
   * //JDOC
   *
   * @param document         PARAM
   * @param element          PARAM
   * @param parentXRElement  PARAM
   */
  public XRElementImpl( Document document,
      Element element,
      XRElement parentXRElement ) {

    this();
    _document = document;
    _element = element;
    _xrParent = parentXRElement;
  }


  /** Constructor for the XRElementImpl object */
  private XRElementImpl() {
    _matchedStyles = new ArrayList();
  }


  /**
   * Returns the parent DOM Document
   *
   * @return   Returns
   */
  public Document parentDocument() {
    return _document;
  }


  /**
   * Returns the parent DOM Element for the enclosed DOM Element, null if this is the root Document Element.
   *
   * @return   Returns
   */
  public Element parentDOMElement() {
    return (Element)_element.getParentNode();
  }


  /**
   * Returns the enclosed DOM Element.
   *
   * @return   Returns
   */
  public Element domElement() {
    return _element;
  }


  /**
   * Returns the parent XRElement, will be null if this is the root.
   *
   * @return   Returns
   */
  public XRElement parentXRElement() {
    return _xrParent;
  }


  /**
   * Returns the text content of the DOM Element.
   *
   * @return   Returns
   */
   // ASK: is this useful?
  public String content() {
    String rtn = "";

    if ( ( _element != null ) && ( _element.getChildNodes().getLength() > 0 ) ) {
      rtn = _element.getChildNodes().item( 0 ).getNodeValue();
    }

    return rtn;
  }


  /**
   * //JDOC 
   * 
   *
   * @return   Returns
   */
   //ASK: synchronized?
  public synchronized XRDerivedStyle derivedStyle() {
    if ( _derivedStyle == null ) {
      _derivedStyle = new XRDerivedStyleImpl( this, _matchedStyles.iterator() );
    }
    return _derivedStyle;
  }


  /**
   * Returns an iterator of the styles matched to this XRElement, in the order added to the XRElement.
   *
   * @return   Returns
   */
  public Iterator applicableStyles() {
    return _matchedStyles.iterator();
  }


  /**
   * Associates an XRStyleRule as matched with the XRElement. 
   *
   * @param style  The feature to be added to the MatchedStyle attribute
   */
  public void addMatchedStyle( XRStyleRule style ) {
    _matchedStyles.add( style );
  }

} // end class


// :folding=indent:collapseFolds=2:
