/*
 * {{{ header & license
 * XRStyleRule.java
 * Copyright (c) 2004 Patrick Wright
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

import org.w3c.css.sac.SelectorList;


/**
 * A rule defining a styling for elements--has a String selector used for
 * matching to elements.
 *
 * @author   Patrick Wright
 */
public interface XRStyleRule extends XRSheetRule {
    /**
     * The selector text as a String, as supplied in construction. Normally
     * multi-value selectors (comma-sep) would be broken into more than one
     * style.
     *
     * @return   Returns
     */
    String cssSelectorText();


    /**
     * The specificity of this selector. See CSS2 spec section 6.4.3
     *
     * @return   Returns
     */
    int selectorSpecificity();


    /**
     * Description of the Method
     *
     * @return   Returns
     */
    SelectorList selectorsAsSACList();

}// end interface

/*
 * $Id$
 *
 * $Log$
 * Revision 1.2  2004/10/23 13:03:47  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */

