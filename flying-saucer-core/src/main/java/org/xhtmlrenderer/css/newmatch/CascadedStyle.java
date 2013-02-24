/*
 * CascadedStyle.java
 * Copyright (c) 2004, 2005 Patrick Wright, Torbjoern Gannholm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
package org.xhtmlrenderer.css.newmatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;


/**
 * Holds a set of {@link org.xhtmlrenderer.css.sheet.PropertyDeclaration}s for
 * each unique CSS property name. What properties belong in the set is not
 * determined, except that multiple entries are resolved into a single set using
 * cascading rules. The set is cascaded during instantiation, so once you have a
 * CascadedStyle, the PropertyDeclarations you retrieve from it will have been
 * resolved following the CSS cascading rules. Note that this class knows
 * nothing about CSS selector-matching rules. Before creating a CascadedStyle,
 * you will need to determine which PropertyDeclarations belong in the set--for
 * example, by matching {@link org.xhtmlrenderer.css.sheet.Ruleset}s to {@link
 * org.w3c.dom.Document} {@link org.w3c.dom.Element}s via their selectors. You
 * can get individual properties by using {@link #propertyByName(CSSName)} or an
 * {@link java.util.Iterator} of properties with {@link
 * #getCascadedPropertyDeclarations()}. Check for individual property assignments
 * using {@link #hasProperty(CSSName)}. A CascadedStyle is immutable, as
 * properties can not be added or removed from it once instantiated.
 *
 * @author Torbjoern Gannholm
 * @author Patrick Wright
 */
public class CascadedStyle {
    /**
     * Map of PropertyDeclarations, keyed by {@link CSSName}
     */
    private Map cascadedProperties;
    
    private String fingerprint;
    
    /**
     * Creates a <code>CascadedStyle</code>, setting the display property to
     * to the value of the <code>display</code> parameter.  
     */
    public static CascadedStyle createAnonymousStyle(IdentValue display) {
        CSSPrimitiveValue val = new PropertyValue(display);
        
        List props = Collections.singletonList(
                new PropertyDeclaration(CSSName.DISPLAY, val, true, StylesheetInfo.USER));
        
        return new CascadedStyle(props.iterator());
    }
    
    /**
     * Creates a <code>CascadedStyle</code> using the provided property
     * declarations.  It is used when a box requires a style that does not
     * correspond to anything in the parsed stylesheets.
     * @param decls An array of PropertyDeclaration objects created with 
     * {@link #createLayoutPropertyDeclaration(CSSName, IdentValue)}
     * @see #createLayoutPropertyDeclaration(CSSName, IdentValue)
     */
    public static CascadedStyle createLayoutStyle(PropertyDeclaration[] decls) {
        return new CascadedStyle(Arrays.asList(decls).iterator());
    }
    
    public static CascadedStyle createLayoutStyle(List decls) {
        return new CascadedStyle(decls.iterator());
    }    
    
    /**
     * Creates a <code>CascadedStyle</code> using style information from
     * <code>startingPoint</code> and then adding the property declarations
     * from <code>decls</code>.
     * @param decls An array of PropertyDeclaration objects created with 
     * {@link #createLayoutPropertyDeclaration(CSSName, IdentValue)}
     * @see #createLayoutPropertyDeclaration(CSSName, IdentValue)
     */
    public static CascadedStyle createLayoutStyle(
            CascadedStyle startingPoint, PropertyDeclaration[] decls) {
        return new CascadedStyle(startingPoint, Arrays.asList(decls).iterator());
    }

    /**
     * Creates a <code>PropertyDeclaration</code> suitable for passing to
     * {@link #createLayoutStyle(PropertyDeclaration[])} or
     * {@link #createLayoutStyle(CascadedStyle, PropertyDeclaration[])}
     */
    public static PropertyDeclaration createLayoutPropertyDeclaration(
            CSSName cssName, IdentValue display) {
        CSSPrimitiveValue val = new PropertyValue(display);
        // Urk... kind of ugly, but we really want this value to be used
        return new PropertyDeclaration(cssName, val, true, StylesheetInfo.USER);
    }

    /**
     * Constructs a new CascadedStyle, given an {@link java.util.Iterator} of
     * {@link org.xhtmlrenderer.css.sheet.PropertyDeclaration}s already sorted
     * by specificity of the CSS selector they came from. The Iterator can have
     * multiple PropertyDeclarations with the same name; the property cascade
     * will be resolved during instantiation, resulting in a set of
     * PropertyDeclarations. Once instantiated, properties may be retrieved
     * using the normal API for the class.
     *
     * @param iter An Iterator containing PropertyDeclarations in order of
     *             specificity.
     */
    CascadedStyle(java.util.Iterator iter) {
        this();

        addProperties(iter);
    }

    private void addProperties(java.util.Iterator iter) {
        //do a bucket-sort on importance and origin
        //properties should already be in order of specificity
        java.util.List[] buckets = new java.util.List[PropertyDeclaration.IMPORTANCE_AND_ORIGIN_COUNT];
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = new java.util.LinkedList();
        }

        while (iter.hasNext()) {
            PropertyDeclaration prop = (PropertyDeclaration) iter.next();
            buckets[prop.getImportanceAndOrigin()].add(prop);
        }

        for (int i = 0; i < buckets.length; i++) {
            for (java.util.Iterator it = buckets[i].iterator(); it.hasNext();) {
                PropertyDeclaration prop = (PropertyDeclaration) it.next();
                cascadedProperties.put(prop.getCSSName(), prop);
            }
        }
    }
    
    private CascadedStyle(CascadedStyle startingPoint, Iterator props) {
        cascadedProperties = new TreeMap(startingPoint.cascadedProperties);
        
        addProperties(props);
    }


    /**
     * Default constructor with no initialization. Don't use this to instantiate
     * the class, as the class is immutable and this will leave it without any
     * properties.
     */
    private CascadedStyle() {
        cascadedProperties = new TreeMap();
    }

    /**
     * Get an empty singleton, used to negate inheritance of properties
     */
    public static final CascadedStyle emptyCascadedStyle = new CascadedStyle();


    /**
     * Returns true if property has been defined in this style.
     *
     * @param cssName The CSS property name, e.g. "font-family".
     * @return True if the property is defined in this set.
     */
    public boolean hasProperty(CSSName cssName) {
        return cascadedProperties.get( cssName ) != null;
    }


    /**
     * Returns a {@link org.xhtmlrenderer.css.sheet.PropertyDeclaration} by CSS
     * property name, e.g. "font-family". Properties are already cascaded during
     * instantiation, so this will return the actual property (and corresponding
     * value) to use for CSS-based layout and rendering.
     *
     * @param cssName The CSS property name, e.g. "font-family".
     * @return The PropertyDeclaration, if declared in this set, or null
     *         if not found.
     */
    public PropertyDeclaration propertyByName(CSSName cssName) {
        PropertyDeclaration prop = (PropertyDeclaration)cascadedProperties.get(cssName);

        return prop;
    }

    /**
     * Gets the ident attribute of the CascadedStyle object
     *
     * @param cssName PARAM
     * @return The ident value
     */
    public IdentValue getIdent(CSSName cssName) {
        PropertyDeclaration pd = propertyByName(cssName);
        return (pd == null ? null : pd.asIdentValue());
    }


    /**
     * Returns an {@link java.util.Iterator} over the set of {@link
     * org.xhtmlrenderer.css.sheet.PropertyDeclaration}s already matched in this
     * CascadedStyle. For a given property name, there may be no match, in which
     * case there will be no <code>PropertyDeclaration</code> for that property
     * name in the Iterator.
     *
     * @return Iterator over a set of properly cascaded PropertyDeclarations.
     */
    public java.util.Iterator getCascadedPropertyDeclarations() {
        List list = new ArrayList(cascadedProperties.size());
        Iterator iter = cascadedProperties.values().iterator();
        while ( iter.hasNext()) {
            list.add(iter.next());
        }
        return list.iterator();
    }

    public int countAssigned() { return cascadedProperties.size(); }

    public String getFingerprint() {
        if (this.fingerprint == null) {
            StringBuffer sb = new StringBuffer();
            Iterator iter = cascadedProperties.values().iterator();
            while (iter.hasNext()) {
                sb.append(((PropertyDeclaration)iter.next()).getFingerprint());
            }
            this.fingerprint = sb.toString();
        }
        return this.fingerprint;
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.19  2007/10/31 23:14:41  peterbrant
 * Add rudimentary support for @font-face rules
 *
 * Revision 1.18  2007/04/12 12:29:11  peterbrant
 * Properly handle floated tables with captions
 *
 * Revision 1.17  2007/02/20 17:23:15  peterbrant
 * Optimize fingerprint calculation
 *
 * Revision 1.16  2007/02/20 01:17:09  peterbrant
 * Start CSS parser cleanup
 *
 * Revision 1.15  2007/02/07 16:33:14  peterbrant
 * Initial commit of rewritten table support and associated refactorings
 *
 * Revision 1.14  2006/06/15 20:02:39  tobega
 * Using a TreeMap to get properties in sorted order should be able to reduce the size of the caches in CalculatedStyle when styles are the same apart from order of declaration of properties.
 *
 * Revision 1.13  2006/01/09 23:22:24  peterbrant
 * Cache fingerprint after initial creation
 *
 * Revision 1.12  2005/10/20 20:48:02  pdoubleya
 * Updates for refactoring to style classes. CalculatedStyle now has lookup methods to cover all general cases, so propertyByName() is private, which means the backing classes for styling were able to be replaced.
 *
 * Revision 1.11  2005/06/21 08:06:47  pdoubleya
 * Changed to use Map of properties again.
 *
 * Revision 1.10  2005/05/16 13:48:58  tobega
 * Fixe inline border mismatch and started on styling problem in switching between blocks and inlines
 *
 * Revision 1.9  2005/05/08 15:37:26  tobega
 * Fixed up style caching so it really works (internalize CascadedStyles and let each CalculatedStyle keep track of its derived children)
 *
 * Revision 1.8  2005/05/08 13:02:36  tobega
 * Fixed a bug whereby styles could get lost for inline elements, notably if root element was inline. Did a few other things which probably has no importance at this moment, e.g. refactored out some unused stuff.
 *
 * Revision 1.7  2005/04/20 14:13:07  tobega
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2005/01/29 20:22:25  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.5  2005/01/25 14:45:55  pdoubleya
 * Added support for IdentValue mapping on property declarations. On both CascadedStyle and PropertyDeclaration you can now request the value as an IdentValue, for object-object comparisons. Updated 99% of references that used to get the string value of PD to return the IdentValue instead; remaining cases are for pseudo-elements where the PD content needs to be manipulated as a String.
 *
 * Revision 1.4  2005/01/24 19:01:06  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
 * Revision 1.3  2004/11/15 13:40:14  pdoubleya
 * Updated JavaDoc.
 *
 * Revision 1.2  2004/11/15 12:42:22  pdoubleya
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

