/*
 *
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

/** Holds a MatchedProperty for each unique property name */
public class CascadedStyle {

    /** The main Map of MatchedProperties keyed by property name, after cascade takes place. */
    private java.util.Map _cascadedPropertiesByName;

    /**
     * @param forElement  PARAM
     * @param iter        PARAM should contain PropertyDeclaration in order of specificity
     */
    public CascadedStyle(java.util.Iterator iter) {
        this();
        
        //do a bucket-sort on importance and origin
        //properties should already be in order of specificity
        java.util.List[] buckets = new java.util.List[PropertyDeclaration.IMPORTANCE_AND_ORIGIN_COUNT];
        for(int i=0; i<buckets.length; i++) {
            buckets[i] = new java.util.LinkedList();
        }

        while ( iter.hasNext() ) {
            PropertyDeclaration prop = (PropertyDeclaration) iter.next();
            buckets[prop.getImportanceAndOrigin()].add(prop);
        }
        
        for(int i=0; i<buckets.length; i++) {
            for(java.util.Iterator it = buckets[i].iterator(); it.hasNext();) {
                PropertyDeclaration prop = (PropertyDeclaration) it.next();
                //System.err.println("matched "+prop.getName());
                _cascadedPropertiesByName.put(prop.getName(), prop);
            }
        }
    }


    protected CascadedStyle() {
        _cascadedPropertiesByName = new java.util.TreeMap();
    }


    /**
     * Returns true if property has been defined in this style.
     *
     * @param propName  PARAM
     * @return          Returns
     */
    public boolean hasProperty( String propName ) {
        return _cascadedPropertiesByName.get( propName ) != null;
    }


    /**
     * @param propName  PARAM
     * @return          Returns
     */
    public PropertyDeclaration propertyByName( String propName ) {
        PropertyDeclaration prop = (PropertyDeclaration)_cascadedPropertiesByName.get( propName );

        return prop;
    }

    public java.util.Iterator getMatchedPropertyDeclarations() {
        return _cascadedPropertiesByName.values().iterator();
    }    

}

