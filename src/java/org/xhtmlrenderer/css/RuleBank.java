
/* 
 * {{{ header & license 
 * Copyright (c) 2004 Joshua Marinacci 
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; either version 2.1 
 * of the License, or (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. 
 * }}} 
 */

package org.xhtmlrenderer.css;

import org.w3c.dom.Element;
import com.steadystate.css.*;
import com.steadystate.css.parser.*;
import org.w3c.dom.css.*;
import org.w3c.css.sac.*;

public interface RuleBank {
    public void addStyleRule(JStyle rule);
    public CSSStyleDeclaration findRule(Element elem, String property, boolean inherit);

    /**
   * Notes that an Element was already parsed for inline styles; after this call, hasStyleForElement(elem) will return true.
   *
   * @param elem   The DOM Element
   */
   void elementWasParsed( Element elem );

  /**
   * Returns true if this Element was already parsed for internal styles. Elements are tracked by the elementWasParsed() method.
   *
   * @param elem  The Element to check on.
   * @return      Returns
   */
  boolean wasElementParsed( Element elem );    
}
