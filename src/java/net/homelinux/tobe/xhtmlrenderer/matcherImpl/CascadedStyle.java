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

package net.homelinux.tobe.xhtmlrenderer.matcherImpl;

import net.homelinux.tobe.xhtmlrenderer.PropertyDeclaration;


public class CascadedStyle implements net.homelinux.tobe.xhtmlrenderer.CascadedStyle {

    /** The main Map of XRProperties keyed by property name, after cascade/inherit takes place. This is the map we look up properties with. Do NOT call clear() (haha). */
    private java.util.Map _cascadedPropertiesByName;

    /**
     * @param forElement  PARAM
     * @param iter        PARAM should contain PropertyDeclarations in order of specificity
     */
    public CascadedStyle(java.util.Iterator iter) {
        this();
        
        //do a bucket-sort on importance and origin
        //properties should already be in order of specificity
        java.util.List[] buckets = new java.util.List[PropertyDeclaration.USER_IMPORTANT+1];
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

        // but the property may not be defined for this Element
        /* no inherit of declared properties, only computed - if ( prop == null ) {
            
            // if it is inheritable (like color) and we are not root, ask our parent
            // for the value
            //TODO: declared values are not inherited
            if ( CSSName.propertyInherits(propName) && _parent != null ) {
                prop = _parent.propertyByName(propName);
            } else {
                // otherwise, use the initial value (defined by the CSS2 Spec)
                String initialValue = CSSName.initialValue(propName);
                if ( initialValue == null ) {
                    throw new RuntimeException("Property '" + propName + "' has no initial values assigned.");
                }
                initialValue = RuleNormalizer.convertIdent(propName, initialValue);
                final com.pdoubleya.xhtmlrenderer.css.impl.DefaultCSSPrimitiveValue cssval = new com.pdoubleya.xhtmlrenderer.css.impl.DefaultCSSPrimitiveValue(initialValue);
                final String name = propName;
                //a default value should always be absolute?
                prop = new PropertyDeclaration() {
                    public int getImportanceAndOrigin() {
                        return CSS_DEFAULT;
                    }

                    public String getName() {
                        return name;
                    }

                    public org.w3c.dom.css.CSSValue getValue() {
                        return cssval;
                    }
                };
            }
            _cascadedPropertiesByName.put(propName, prop);
        }*/
        return prop;
    }

    public java.util.Iterator getPropertyDeclarations() {
        return _cascadedPropertiesByName.values().iterator();
    }    

}

