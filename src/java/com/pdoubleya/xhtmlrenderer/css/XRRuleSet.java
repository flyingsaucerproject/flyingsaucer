/*
 * {{{ header & license
 * XRRuleSet.java
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
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleSheet;

/**
 * Interface for CSS rules (@ rules, style rules). An XRRule is also a DOM
 * CSSRule. Rules have a sequence which is the order they were found within
 * their stylesheet.
 *
 * @author    Patrick Wright
 * @created   August 1, 2004
 */
public interface XRRuleSet {

  /**
   * An iterator of all XRProperties in this rule.
   *
   * @return   Returns
   */
  Iterator listXRProperties();


  /**
   * Value of a single XRProperty, by name.
   *
   * @param propName  PARAM
   * @return          Returns
   */
  XRProperty propertyByName( String propName );

  /** 
   * Merges two XRRuleSets, combining all properties. This is not used for cascading, rather for
   * two rules defined separately in the same sheet with the same selector. Any properties with the
   * same name in fromRuleSet will replace existing properties with that name in this XRRuleSet.
   */
   void mergeProperties(XRRuleSet fromRuleSet);
  
}// end interface


// :folding=indent:collapseFolds=2:
