/*
 * {{{ header & license
 * XRProperty.java
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


/**
 * A property read from a CSS2 style declaration. The property gives you access
 * to the value (initial, specified, computed, actual). An <code>XRProperty</code>
 * should be considered immutable, even though it is possible to find the <code>CSSValue</code>
 * instance associated with the property and manipulate it. When you have an
 * <code>XRProperty</code> instance, you can either retrieve the value as
 * specified by the user in the CSS using {@link #specifiedValue()}, or the
 * value as resolved for the current environment using {@link #actualValue()}.
 *
 * @author Patrick Wright
 */
// ASK: should Properties have a reference back to their rules?
// ASK: should Properties have a reference back to their stylesheets?
public interface XRProperty {
    /**
     * The name of this property--as supplied in stylesheet, should also be a
     * valid property name in CSSName
     *
     * @return See desc.
     */
    String propertyName();


    /**
     * The value as specified by stylesheet (CSS2 6.1.1)--if the value for the
     * property is relative (e.g. ems or ex values), returns an <code>XRValue</code>
     * with the relative, not computed, value. Generally this is only useful for
     * debugging.
     *
     * @return See desc.
     */
    XRValue specifiedValue();

    /**
     * The specified value, if absolute, or the relative value as absolute, if
     * relative :).
     *
     * @return See desc.
     */
    XRValue computedValue();

    /**
     * The actual value--the computed value when limited by the current
     * presentation environment. (CSS2 6.1.3)
     *
     * @return See desc.
     */
    // TODO: need Context and possibly current Element to determine proportional values (PWW 12-11-04)
    XRValue actualValue();


    /**
     * Deep copy operation. However, any contained SAC instances are not
     * deep-copied.
     *
     * @return A copy of this <code>XRProperty</code>.
     */
    XRProperty copyOf();


    /**
     * Deep copy operation for the purposes of inheriting a computed value. Used
     * when a child element needs the parent element's computed value for a
     * property. The following is true of the copy: 1) is resolved 2) computed
     * value is same as parent's computed 3) actual value is same as parent's
     * actual value. Any contained SAC instances are not deep-copied.
     *
     * @return See desc
     */
    XRProperty copyForInherit();
}// end interface

/*
 * $Id$
 *
 * $Log$
 * Revision 1.5  2004/12/29 10:39:27  tobega
 * Separated current state Context into ContextImpl and the rest into SharedContext.
 *
 * Revision 1.4  2004/11/15 12:42:21  pdoubleya
 * Across this checkin (all may not apply to this particular file)
 * Changed default/package-access members to private.
 * Changed to use XRRuntimeException where appropriate.
 * Began move from System.err.println to std logging.
 * Standard code reformat.
 * Removed some unnecessary SAC member variables that were only used in initialization.
 * CVS log section.
 *
 * Revision 1.3  2004/11/10 04:41:43  tobega
 * no message
 *
 * Revision 1.2  2004/10/23 13:03:46  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */

