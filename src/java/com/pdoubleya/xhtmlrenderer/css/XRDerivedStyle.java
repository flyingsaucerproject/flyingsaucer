/*
 * {{{ header & license
 * XRDerivedStyle.java
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
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import org.joshy.html.Border;

import com.pdoubleya.xhtmlrenderer.css.value.BorderColor;


/**
 * A set of properties that apply to a single Element. A derived style is just
 * like a style but (presumably) has additional information that allows relative
 * properties to be assigned values, e.g. font attributes. Property values are
 * fully resolved when this style is created, so values should be usable on any
 * instance of XRDerivedStyle.
 *
 * @author    Patrick Wright
 * @created   August 1, 2004
 */
 // ASK: marker interface?
public interface XRDerivedStyle extends XRRuleSet {
  /**
   * Convenience property accessor; returns a Border initialized with the
   * four-sided border width. Uses the actual value (computed actual value) for
   * this element.
   *
   * @return   The borderWidth value
   */
  Border getBorderWidth();

  /**
   * Convenience property accessor; returns a Border initialized with the
   * four-sided margin width. Uses the actual value (computed actual value) for
   * this element.
   *
   * @return   The marginWidth value
   */
  Border getMarginWidth();
  
  /**
   * Convenience property accessor; returns a Border initialized with the
   * four-sided padding width. Uses the actual value (computed actual value) for
   * this element.
   *
   * @return   The paddingWidth value
   */
  Border getPaddingWidth();
  
  /**
   * Convenience property accessor; returns a Color initialized with the
   * background color value; Uses the actual value (computed actual value) for
   * this element.
   *
   * @return   The backgroundColor value
   */
  Color getBackgroundColor();

  /**
   * Convenience property accessor; returns a BorderColor initialized with the
   * four-sided border color. Uses the actual value (computed actual value) for
   * this element.
   *
   * @return   The borderColor value
   */
  BorderColor getBorderColor();
  
  /**
   * Convenience property accessor; returns a Color initialized with the
   * foreground color Uses the actual value (computed actual value) for this
   * element.
   *
   * @return   The color value
   */
  Color getColor();

  /**
   * Convenience property accessor; returns the Font to use for text on this
   * element. Uses the actual value (computed actual value) for this element.
   *
   * @return   The font value
   */
  Font getFont(Graphics g);
  
}// end interface


