/*
 * {{{ header & license
 * XRElement.java
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.joshy.html.box.Box;

/**
 * A DOM element that we have wrapped for XR use. In particular, this class
 * represents the relationship between a DOM Element, it's matched styles, it's
 * derived styles (after cascade/inherit) and its visual representation (current. incomplete).
 * This association is necessary because in order to derive all styles, and to compute
 * relative values, we need to have parent/child Element associations available.
 *
 * The intention is that XRElements are instantiated after processing a DOM, and
 * a CSS selector matcher calls <code>addMatchedStyle()</code> for each style that matches the 
 * element. Once all styles are matched, <code>derivedStyle()</code> returns a 
 * XRRuleSet instance with all the applicable properties for the class.
 *
 * 
 *
 * @author    Patrick Wright
 * @created   August 1, 2004
 */
 //ASK: if we are going to use DOM anyway, should this extend Element?
public interface XRElement {
  /**
   * The DOM owner document for this element.
   *
   * @return   Returns
   */
  Document parentDocument();


  /**
   * The DOM Element that owns the wrapped element here.
   *
   * @return   Returns
   */
  Element parentDOMElement();


  /**
   * The DOM Element we are wrapping.
   *
   * @return   Returns
   */
  Element domElement();


  /**
   * Our parent XRElement.
   *
   * @return   Returns
   */
  XRElement parentXRElement();


  /**
   * A derived set of properties for this element, taken from the matched styles.
   *
   * @return   Returns
   */
  XRDerivedStyle derivedStyle();


  /**
   * Iterator over all the styles matched to this element.
   *
   * @return   Returns
   */
  Iterator applicableStyles();


  /**
   * Associates a style rule with this element--selector matches. Note this should
   * be a pure-selector match, regardless of cascade or other such rules. Those are
   * processed during derivation. The sequence in which they are added is not important,
   * as long as the rule itself has sequencing information (see XRStyleRule for details).
   *
   * @param style  
   */
  void addMatchedStyle( XRStyleRule style );
}// end interface


