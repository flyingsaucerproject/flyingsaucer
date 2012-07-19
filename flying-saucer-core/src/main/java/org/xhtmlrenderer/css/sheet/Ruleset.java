/*
 * Ruleset.java
 * Copyright (c) 2004, 2005 Patrick Wright, Torbjoern Gannholm
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
package org.xhtmlrenderer.css.sheet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.xhtmlrenderer.css.newmatch.Selector;


/**
 * @author Torbjoern Gannholm
 * @author Patrick Wright
 */
public class Ruleset {
    private int _origin;
    private java.util.List _props;

    private List _fsSelectors = new ArrayList();

    public Ruleset(int orig) {
        _origin = orig;
        _props = new LinkedList();
        _fsSelectors = new LinkedList();
    }

    /**
     * Returns an Iterator of PropertyDeclarations pulled from this
     * CSSStyleRule.
     *
     * @return The propertyDeclarations value
     */
    public List getPropertyDeclarations() {
        return Collections.unmodifiableList(_props);
    }

    public void addProperty(PropertyDeclaration decl) {
        _props.add(decl);
    }
    
    public void addAllProperties(List props) {
        _props.addAll(props);
    }
    
    public void addFSSelector(Selector selector) {
        _fsSelectors.add(selector);
    }
    
    public List getFSSelectors() {
        return _fsSelectors;
    }
    
    public int getOrigin() {
        return _origin;
    }

}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.17  2007/08/19 22:22:54  peterbrant
 * Merge R8pbrant changes to HEAD
 *
 * Revision 1.16.2.1  2007/07/09 22:18:02  peterbrant
 * Begin work on running headers and footers and named pages
 *
 * Revision 1.16  2007/02/20 01:17:11  peterbrant
 * Start CSS parser cleanup
 *
 * Revision 1.15  2007/02/19 14:53:38  peterbrant
 * Integrate new CSS parser
 *
 * Revision 1.14  2006/07/26 18:05:05  pdoubleya
 * Clean exception throw.
 *
 * Revision 1.13  2006/05/08 21:36:03  pdoubleya
 * Log and skip properties we can't parse into declarations...
 *
 * Revision 1.12  2005/12/30 01:32:41  peterbrant
 * First merge of parts of pagination work
 *
 * Revision 1.11  2005/10/20 20:48:05  pdoubleya
 * Updates for refactoring to style classes. CalculatedStyle now has lookup methods to cover all general cases, so propertyByName() is private, which means the backing classes for styling were able to be replaced.
 *
 * Revision 1.10  2005/10/15 23:39:15  tobega
 * patch from Peter Brant
 *
 * Revision 1.9  2005/07/14 17:43:39  joshy
 * fixes for parser access exceptions when running in a sandbox (webstart basically)
 *
 * Revision 1.8  2005/06/16 07:24:46  tobega
 * Fixed background image bug.
 * Caching images in browser.
 * Enhanced LinkListener.
 * Some house-cleaning, playing with Idea's code inspection utility.
 *
 * Revision 1.7  2005/01/29 20:19:21  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.6  2005/01/29 12:08:23  pdoubleya
 * Added constructor for SelectorList/PD List, for possible use of our own SAC DocumentHandler in the future.
 *
 * Revision 1.5  2005/01/24 19:01:08  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
 * Revision 1.4  2005/01/24 14:36:30  pdoubleya
 * Mass commit, includes: updated for changes to property declaration instantiation, and new use of DerivedValue. Removed any references to older XR... classes (e.g. XRProperty). Cleaned imports.
 *
 * Revision 1.3  2004/11/15 12:42:23  pdoubleya
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

