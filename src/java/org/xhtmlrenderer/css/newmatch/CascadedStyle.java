/*
 * CascadedStyle.java
 * Copyright (c) 2004 Patrick Wright, Torbjörn Gannholm
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
package org.xhtmlrenderer.css.newmatch;

import java.util.*;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;


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
 * #getMatchedPropertyDeclarations()}. Check for individual property assignments
 * using {@link #hasProperty(CSSName)}. A CascadedStyle is immutable, as
 * properties can not be added or removed from it once instantiated.
 *
 * @author   Torbjörn Gannholm
 * @author   Patrick Wright
 */
public class CascadedStyle {

    /**
     * Array of PropertyDeclarations, indexed by {@link CSSName#getAssignedID()}
     */
    private PropertyDeclaration[] _cascadedPropertiesByID;

    /**
     * Constructs a new CascadedStyle, given an {@link java.util.Iterator} of
     * {@link org.xhtmlrenderer.css.sheet.PropertyDeclaration}s already sorted
     * by specificity of the CSS selector they came from. The Iterator can have
     * multiple PropertyDeclarations with the same name; the property cascade
     * will be resolved during instantiation, resulting in a set of
     * PropertyDeclarations. Once instantiated, properties may be retrieved
     * using the normal API for the class.
     *
     * @param iter  An Iterator containing PropertyDeclarations in order of
     *      specificity.
     */
    public CascadedStyle( java.util.Iterator iter ) {
        this();

        //do a bucket-sort on importance and origin
        //properties should already be in order of specificity
        java.util.List[] buckets = new java.util.List[PropertyDeclaration.IMPORTANCE_AND_ORIGIN_COUNT];
        for ( int i = 0; i < buckets.length; i++ ) {
            buckets[i] = new java.util.LinkedList();
        }

        while ( iter.hasNext() ) {
            PropertyDeclaration prop = (PropertyDeclaration)iter.next();
            buckets[prop.getImportanceAndOrigin()].add( prop );
        }

        for ( int i = 0; i < buckets.length; i++ ) {
            for ( java.util.Iterator it = buckets[i].iterator(); it.hasNext();  ) {
                PropertyDeclaration prop = (PropertyDeclaration)it.next();
                _cascadedPropertiesByID[prop.getCSSName().getAssignedID()] = prop;
            }
        }
    }


    /**
     * Default constructor with no initialization. Don't use this to instantiate
     * the class, as the class is immutable and this will leave it without any
     * properties.
     */
    protected CascadedStyle() {
        // CLEAN_cascadedPropertiesByName = new java.util.TreeMap(CSSName.getComparator());
        _cascadedPropertiesByID = new PropertyDeclaration[CSSName.countCSSNames()];
    }


    /**
     * Returns true if property has been defined in this style.
     *
     * @param cssName  The CSS property name, e.g. "font-family".
     * @return          True if the property is defined in this set.
     */
    public boolean hasProperty( CSSName cssName ) {
        //return _cascadedPropertiesByName.get( cssName ) != null;
        return _cascadedPropertiesByID[cssName.getAssignedID()] != null;
    }


    /**
     * Returns a {@link org.xhtmlrenderer.css.sheet.PropertyDeclaration} by CSS
     * property name, e.g. "font-family". Properties are already cascaded during
     * instantiation, so this will return the actual property (and corresponding
     * value) to use for CSS-based layout and rendering.
     *
     * @param cssName  The CSS property name, e.g. "font-family".
     * @return          The PropertyDeclaration, if declared in this set, or
     *      null if not found.
     */
    public PropertyDeclaration propertyByName( CSSName cssName ) {
        PropertyDeclaration prop = _cascadedPropertiesByID[cssName.getAssignedID()];

        return prop;
    }

    public IdentValue getIdent(CSSName cssName) {
        PropertyDeclaration pd = propertyByName(cssName);
        return ( pd == null ? null : pd.asIdentValue());
    }


    /**
     * Returns an {@link java.util.Iterator} over the set of {@link
     * org.xhtmlrenderer.css.sheet.PropertyDeclaration}s already matched in this
     * CascadedStyle. For a given property name, there may be no match, in which
     * case there will be no <code>PropertyDeclaration</code> for that property name in the Iterator.
     *
     * @return   Iterator over a set of properly cascaded PropertyDeclarations.
     */
    public java.util.Iterator getMatchedPropertyDeclarations() {
        List list = new ArrayList(_cascadedPropertiesByID.length);
        for (int i = 0; i < _cascadedPropertiesByID.length; i++) {
            PropertyDeclaration propertyDeclaration = _cascadedPropertiesByID[i];
            if ( propertyDeclaration == null ) continue;
            list.add(propertyDeclaration);
        }
        return list.iterator();
    }

}// end class

/*
 * $Id$
 *
 * $Log$
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

