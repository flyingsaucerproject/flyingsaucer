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
 * can get individual properties by using {@link #propertyByName(String)} or an
 * {@link java.util.Iterator} of properties with {@link
 * #getMatchedPropertyDeclarations()}. Check for individual property assignments
 * using {@link #hasProperty(String)}. A CascadedStyle is immutable, as
 * properties can not be added or removed from it once instantiated.
 *
 * @author   Torbjörn Gannholm
 * @author   Patrick Wright
 */
public class CascadedStyle {
    /**
     * The main Map of MatchedProperties keyed by CSS property name, after
     * cascade takes place.
     */
    private java.util.Map _cascadedPropertiesByName;

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
                //System.err.println("matched "+prop.getName());
                _cascadedPropertiesByName.put( prop.getName(), prop );
            }
        }
    }


    /**
     * Default constructor with no initialization. Don't use this to instantiate
     * the class, as the class is immutable and this will leave it without any
     * properties.
     */
    protected CascadedStyle() {
        _cascadedPropertiesByName = new java.util.TreeMap();
    }


    /**
     * Returns true if property has been defined in this style.
     *
     * @param propName  The CSS property name, e.g. "font-family".
     * @return          True if the property is defined in this set.
     */
    public boolean hasProperty( String propName ) {
        return _cascadedPropertiesByName.get( propName ) != null;
    }


    /**
     * Returns a {@link org.xhtmlrenderer.css.sheet.PropertyDeclaration} by CSS
     * property name, e.g. "font-family". Properties are already cascaded during
     * instantiation, so this will return the actual property (and corresponding
     * value) to use for CSS-based layout and rendering.
     *
     * @param propName  The CSS property name, e.g. "font-family".
     * @return          The PropertyDeclaration, if declared in this set, or
     *      null if not found.
     */
    public PropertyDeclaration propertyByName( String propName ) {
        PropertyDeclaration prop = (PropertyDeclaration)_cascadedPropertiesByName.get( propName );

        return prop;
    }

    /**
     * Returns an {@link java.util.Iterator} over the set of {@link
     * org.xhtmlrenderer.css.sheet.PropertyDeclaration}s in this CascadedStyle.
     *
     * @return   Iterator over a set of properly cascaded PropertyDeclarations.
     */
    public java.util.Iterator getMatchedPropertyDeclarations() {
        return _cascadedPropertiesByName.values().iterator();
    }

}// end class

/*
 * $Id$
 *
 * $Log$
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

