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

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.io.Reader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSValue;
import org.xhtmlrenderer.css.value.BorderColor;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;

import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.extend.AttributeResolver;
import org.xhtmlrenderer.extend.NamespaceHandler;


/**
 * <p>
 *
 * A StyleReference is a convenience front-end to look up CSS2 style rules and
 * properties. The storage of properties takes place through the parse()
 * methods. Storage may be through an internal RuleBank or some other mechanism.
 * Resolution of property cascade and inheritance happens when properties are
 * looked up. Matching of styles to elements happens when matchStyles(Document)
 * is called. The order to use this is then: parse()* - matchStyles() -
 * getProperty(). If styles are added after the fact then matchStyles() should
 * be called again.</p> <p>
 *
 * StyleReference provides a number of easy-to-use access methods for finding
 * the value of a property assigned to an Element based on loaded StyleSheets.
 * The RuleBank holding these properties is pluggable so different caching and
 * lookup schemes are possible for StyleSheets.</p> <p>
 *
 * All the property accessor method (getZZZ()) search against properties already
 * matched to Elements. Several offer the option of searching for the property
 * matched to the Element's ancestors if it is not matched to the Element
 * itself. The RuleBank is used to determine the Element-property match.
 * Property names are always the CSS2 property names, as strings.</p> <p>
 *
 * NOTE: pulled from CSSBank and CSSAccessor to have a common ancestor interface
 * </p>
 *
 * @author   Patrick Wright
 */
public interface StyleReference {
    /**
     * Returns the background Color assigned to an Element
     *
     * @param elem  The DOM element to find the property for.
     * @return      The Color property
     */
    Color getBackgroundColor( Element elem );


    /**
     * Returns the border Color assigned to an Element
     *
     * @param elem  The DOM element to find the property for.
     * @return      The Color property
     */
    BorderColor getBorderColor( Element elem );


    /**
     * Returns the border width (all sides) assigned to an Element
     *
     * @param elem  The DOM element to find the property for.
     * @return      The Border property (for widths)
     */
    Border getBorderWidth( Element elem );


    /**
     * Returns the foreground Color assigned to an Element, searching ancestor
     * Elements for an inheritable value if not found on the Element.
     *
     * @param elem  The DOM element to find the property for.
     * @return      The foreground Color property
     */
    Color getColor( Element elem );


    /**
     * Returns the foreground Color assigned to an Element, searching ancestor
     * Elements for an inheritable value if requested.
     *
     * @param elem     The DOM element to find the property for.
     * @param inherit  If true and property not found on this element, searches
     *      through element ancestors for property
     * @return         The foreground Color property
     */
    Color getColor( Element elem, boolean inherit );


    /**
     * Returns the a property assigned to an Element that can be interpreted as
     * a Point with floating-point positioning, and, if not found and inherit is
     * true, searches for an inheritable property by that name assigned to
     * parent and ancestor elements.
     *
     * @param elem     The DOM element to find the property for.
     * @param prop     The property name
     * @param inherit  If true and property not found on this element, searches
     *      through element ancestors for property
     * @return         The named property as a Point
     */
    Point getFloatPairProperty( Element elem, String prop, boolean inherit );


    /**
     * Returns the value of a property matched to an element cast as a float,
     * searching ancestor Elements for an inheritable value if not found on the
     * Element.
     *
     * @param elem  The DOM Element to find the property for
     * @param prop  The property name
     * @return      The named property as a float
     */
    float getFloatProperty( Element elem, String prop );


    /**
     * Returns the value of a property matched to an element cast as a float,
     * inheriting the value from the nearest ancestor if requested
     *
     * @param elem     The DOM Element to find the property for
     * @param prop     The property name
     * @param inherit  If true, searches ancestor Elements for the property if
     *      not defined on this Element.
     * @return         The named property as a float
     */
    float getFloatProperty( Element elem, String prop, boolean inherit );


    /**
     * Returns the value of a property matched to an element cast as a float,
     * using the parent float value to determine relative values if requested
     * (e.g. if the property is a percentage), and inheriting the value if
     * necessary.
     *
     * @param elem          The DOM Element to find the property for
     * @param prop          The property name
     * @param parent_value  The Element's parent value for the same property
     * @return              The named property as a float
     */
    float getFloatProperty( Element elem, String prop, float parent_value );


    /**
     * Returns the value of a property matched to an element cast as a float,
     * using the parent float value to determine relative values if requested
     * (e.g. if the property is a percentage), inheriting the value if
     * requested.
     *
     * @param elem          The DOM Element to find the property for
     * @param prop          The property name
     * @param parent_value  The Element's parent value for the same property
     * @param inherit       If true, inherits the value from the Element's
     *      parent
     * @return              The named property as a float
     */
    float getFloatProperty( Element elem, String prop, float parent_value, boolean inherit );


    /**
     * Same as <code>getFloatProperty(Element, String, float, boolean)</code>,
     * but for Node elements
     *
     * @param node          The DOM Node to find the property for
     * @param prop          The property name
     * @param parent_value  The Node's parent value for the same property
     * @param inherit       If true, inherits the value from the Node's parent
     * @return              The named property as a float
     */
    float getFloatProperty( Node node, String prop, float parent_value, boolean inherit );


    /**
     * Returns the margin width (all sides) assigned to an Element
     *
     * @param elem  The DOM element to find the property for.
     * @return      The margin property as a Border (for widths)
     */
    Border getMarginWidth( Element elem );


    /**
     * Returns the padding width (all sides) assigned to an Element
     *
     * @param elem  The DOM element to find the property for.
     * @return      The padding property as a Border (for widths)
     */
    Border getPaddingWidth( Element elem );


    /**
     * Returns the value of a property as a W3C CSSValue instance, inheriting
     * from the parent element if requested.
     *
     * @param elem     The DOM Element to find the property for
     * @param prop     The property name
     * @param inherit  If true, inherits the value from the Element's parent
     * @return         The property value as CSSValue
     */
    CSSValue getProperty( Element elem, String prop, boolean inherit );


    /**
     * Returns the value of a property as a String array, for example, for
     * font-family declarations, inheriting the property if necessary.
     *
     * @param elem  The DOM Element to find the property for
     * @param prop  The property name
     * @return      The property value as String array
     */
    String[] getStringArrayProperty( Element elem, String prop );


    /**
     * Returns the value of a property as a String, inheriting the property if
     * necessary.
     *
     * @param elem  The DOM Element to find the property for
     * @param prop  The property name
     * @return      The property value as String
     */
    String getStringProperty( Element elem, String prop );


    /**
     * Returns the value of a property as a String, inheriting the value from
     * the Element's parent if requested.
     *
     * @param elem     The DOM Element to find the property for
     * @param prop     The property name
     * @param inherit  If true, inherits the property from the Element's parent
     *      if necessary.
     * @return         The property value as String
     */
    String getStringProperty( Element elem, String prop, boolean inherit );


    /**
     * Returns the value of a property as a String from a DOM Node instance,
     * searching ancestor Elements for an inheritable value if not found on the
     * Element.
     *
     * @param node  The DOM Node to find the property for
     * @param prop  The property name
     * @return      The property value as String
     */
    String getStringProperty( Node node, String prop );


    /**
     * Returns the value of a property as a String, inheriting the value from
     * the Node's parent if requested.
     *
     * @param node     The DOM Node to find the property for.
     * @param prop     The property name
     * @param inherit  If true, inherits the property from the Element's parent
     *      if necessary
     * @return         The property value as String
     */
    String getStringProperty( Node node, String prop, boolean inherit );


    /**
     * Checks whether a property is defined at all for an Element, inherited or
     * not.
     *
     * @param elem  The DOM Element to find the property for
     * @param prop  The property name
     * @return      True if the Element, or an ancestor, has the property
     *      defined.
     */
    boolean hasProperty( Element elem, String prop );


    /**
     * Checks whether a property is defined at all for an Element, searching
     * ancestor Elements for the property if requested.
     *
     * @param elem     The DOM Element to find the property for
     * @param prop     The property name
     * @param inherit  If true, searches ancestors for the Element for the
     *      property as well.
     * @return         True if the Element has the property defined.
     */
    boolean hasProperty( Element elem, String prop, boolean inherit );


    /**
     * Checks whether a property is defined at all for an Node, searching
     * ancestor Node for the property if requested.
     *
     * @param node     The DOM Node to find the property for
     * @param prop     The property name
     * @param inherit  If true, searches ancestors for the Node for the property
     *      as well.
     * @return         True if the Node has the property defined.
     */
    boolean hasProperty( Node node, String prop, boolean inherit );


    /**
     * <p>
     *
     * Attempts to match any loaded to Elements in the supplied Document, using
     * CSS2 matching rules on selection. This should be called after all
     * stylesheets and styles are loaded, but before any properties are
     * retrieved. </p>
     *
     * @param document  PARAM
     */
    //void matchStyles( Document document );


    /**
     * Parses the CSS style sheet enclosed by the Reader into style information,
     * and loads this into the associated RuleBank.
     *
     * @param reader           A Reader from which to read the style
     *      information.
     * @exception IOException  Throws
     */
    //void parse( Reader reader )
    //    throws IOException;


    /**
     * Parses the CSS style sheet enclosed by the Reader into style information,
     * and loads this into the associated RuleBank.
     *
     * @param reader           A Reader from which to read the style
     *      information.
     * @param origin           The origin of the enclosed style information--an
     *      int constant from XRStyleSheet, e.g. {@link
     *      org.xhtmlrenderer.css.XRStyleSheet#AUTHOR}. Used to determine
     *      precedence of rules derived from the parse sheet.
     * @exception IOException  Throws
     */
    //void parse( Reader reader, int origin )
    //    throws IOException;

    /**
     * Parses the CSS style information from the source parameter, and loads
     * these rules into the associated RuleBank.
     *
     * @param source           A String containing CSS style rules
     * @exception IOException  Throws
     */
    //void parse( String source )
    //    throws IOException;

    /**
     * Same as {@link #parse(Reader, int)} for a String datasource.
     *
     * @param source           A String containing CSS style rules
     * @param origin           See {@link #parse(Reader, int)}
     * @exception IOException  {@link #parse(Reader, int)}
     */
    //public void parse( String source, int origin )
    //    throws IOException;

    /**
     * Parses the CSS style information from a <?xml-stylesheet?> PI and loads
     * these rules into the associated RuleBank.
     *
     * @param root             Root of the document for which to search for link
     *      tags.
     * @exception IOException  Throws
     */
    //public void parseDeclaredStylesheets( Element root )
    //    throws IOException;

    /**
     * Parses the CSS style information from a "
     * <link> " Element (for example in XHTML), and loads these rules into the
     * associated RuleBank.
     *
     * @param elem             The Element from which to pull a style attribute.
     * @exception IOException  Throws
     */
    //void parseLinkedStyles( Element elem )
    //    throws IOException;

    /**
     * Parses the CSS style information from a "<style>" Element (for example in
     * XHTML), and loads these rules into the associated RuleBank.
     *
     * @param elem             The Element from which to pull a style attribute.
     * @exception IOException  Throws
     */
    //void parseInlineStyles( Element elem )
    //    throws IOException;


    /**
     * Parses the CSS style information from the inline "style" attribute on the
     * DOM Element, and loads these rules into the associated RuleBank,
     * automatically associating those styles as matched to the Element.
     *
     * @param elem             The Element from which to pull a style attribute.
     * @exception IOException  Throws
     */
    //void parseElementStyling( Element elem )
    //    throws IOException;
    
    /**
     * List the derived (calculated) properties for an Element.
     * with the property name as key and the cssValue as value.
     */
    public java.util.Map getDerivedPropertiesMap(Element e);
    
    
    /**
     * Does what is needed to handle a new document (called when a new document is loaded).
     *
     * @param context   layout context
     * @param nsh       NamespaceHandler for the document
     * @param ar        AttributeResolver for the document
     * @param doc       DOM document
     **/
    public void setDocumentContext(Context context, NamespaceHandler nsh, AttributeResolver ar, Document doc);
    
    /**
     * Handle the pseudoElements, may return null
     */
    public CascadedStyle getPseudoElementStyle(Element e, String pseudoElement);
    
    /**
     * get the CalculatedStyle once for the element, then query that
     */
    public CalculatedStyle getStyle(Element e);
    
    /**
     * Handle a restyle because of a change in hover state. If the element (and its children) was restyled
     *because of this, true is returned
     */
    public boolean wasHoverRestyled(Element e);

}// end interface

/*
 * $Id$
 *
 * $Log$
 * Revision 1.9  2004/11/09 23:57:41  tobega
 * Added hook to StyleReference for dynamic hover restyling
 *
 * Revision 1.8  2004/11/08 23:15:55  tobega
 * Changed pseudo-element styling to just return CascadedStyle
 *
 * Revision 1.7  2004/11/08 08:22:15  tobega
 * Added support for pseudo-elements
 *
 * Revision 1.6  2004/11/07 01:31:38  tobega
 * Added hooks for handling First-letter pseudo-element
 *
 * Revision 1.5  2004/11/07 01:17:55  tobega
 * DOMInspector now works with any StyleReference
 *
 * Revision 1.4  2004/11/04 21:50:54  tobega
 * Preparation for new matching/styling code
 *
 * Revision 1.3  2004/10/23 13:03:46  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */

