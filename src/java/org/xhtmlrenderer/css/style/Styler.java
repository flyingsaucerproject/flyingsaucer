/*
 * Styler.java
 * Copyright (c) 2004, 2005 Torbjörn Gannholm
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
 *
 */
package org.xhtmlrenderer.css.style;

import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.layout.SharedContext;


/**
 * @author   Torbjörn Gannholm
 */
public class Styler {
    /** Description of the Field  */
    private java.util.HashMap _styleMap = new java.util.HashMap();

    /**
     * Cache styles that have the same parent style and same cascaded properties
     */
    private java.util.HashMap _styleCache = new java.util.HashMap();

    /** Creates a new instance of Styler  */
    public Styler() { }

    /**
     * Gets the derivedStyle attribute of the Styler object
     *
     * @param parent   PARAM
     * @param matched  PARAM
     * @return         The derivedStyle value
     */
    public CalculatedStyle getDerivedStyle( CalculatedStyle parent, CascadedStyle matched, SharedContext context ) {
        CalculatedStyle cs = null;
        StringBuffer sb = new StringBuffer();
        sb.append( parent.hashCode() ).append( ":" ).append( matched.hashCode() );
        String fingerprint = sb.toString();
        cs = (CalculatedStyle)_styleCache.get( fingerprint );

        if ( cs == null ) {
            cs = new CalculatedStyle( parent, matched, context );
            _styleCache.put( fingerprint, cs );
        }
        return cs;
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.15  2005/03/24 23:13:23  pdoubleya
 * Added use of SharedContext (Kevin).
 *
 * Revision 1.14  2005/01/29 20:19:22  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.13  2005/01/24 14:36:31  pdoubleya
 * Mass commit, includes: updated for changes to property declaration instantiation, and new use of DerivedValue. Removed any references to older XR... classes (e.g. XRProperty). Cleaned imports.
 *
 * Revision 1.12  2005/01/03 23:40:40  tobega
 * Cleaned out unnecessary styling/matching code. styling/matching is now called during boxing/rendering rather than as a separate stage.
 *
 * Revision 1.11  2004/12/12 02:56:36  tobega
 * Making progress
 *
 * Revision 1.10  2004/12/09 21:18:51  tobega
 * precaution: code still works
 *
 * Revision 1.9  2004/12/05 18:11:36  tobega
 * Now uses style cache for pseudo-element styles. Also started preparing to replace inline node handling with inline content handling.
 *
 * Revision 1.8  2004/11/15 12:42:23  pdoubleya
 * Across this checkin (all may not apply to this particular file)
 * Changed default/package-access members to private.
 * Changed to use XRRuntimeException where appropriate.
 * Began move from System.err.println to std logging.
 * Standard code reformat.
 * Removed some unnecessary SAC member variables that were only used in initialization.
 * CVS log section.
 *
 *
 */

