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

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;
import org.w3c.css.sac.SimpleSelector;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSStyleDeclaration;
import com.steadystate.css.parser.CSSOMParser;


/*
 * ============ CSS searching and cascading code ================
 */
/**
 * Description of the Class
 *
 * @author   empty
 */
public class RuleFinder implements RuleBank {

    /** Description of the Field */
    List styles;

    /** Description of the Field */
    List parsedElements;

    /** Constructor for the RuleFinder object */
    public RuleFinder() {

        styles = new ArrayList();

        parsedElements = new ArrayList();
    }

    /**
     * CLN: added (PWW 13/08/04) replaces style_nodes.contains(elem);
     *
     * @param elem  PARAM
     * @return      Returns
     */
    public boolean wasElementParsed( Element elem ) {
        return this.parsedElements.contains( elem );
    }

    /**
     * CLN: added (PWW 13/08/04) replaces style_nodes.add(elem);
     *
     * @param elem  PARAM
     */
    public void elementWasParsed( Element elem ) {
        this.parsedElements.add( elem );
    }


    /**
     * Adds a feature to the StyleRule attribute of the RuleFinder object
     *
     * @param rule  The feature to be added to the StyleRule attribute
     */
    public void addStyleRule( JStyle rule ) {

        styles.add( rule );

    }




    /**
     * Description of the Method
     *
     * @param elem      PARAM
     * @param property  PARAM
     * @param inherit   PARAM
     * @return          Returns
     */
    public CSSStyleDeclaration findRule( Element elem, String property, boolean inherit ) {

        // loop through the styles in reverse order

        for ( int s = styles.size() - 1; s >= 0; s-- ) {

            JStyle style = (JStyle)styles.get( s );

            // loop through each selector for the style

            for ( int i = 0; i < style.selector_list.getLength(); i++ ) {

                Selector sel = style.selector_list.item( i );

                // if the selector matches

                if ( matchSelector( sel, elem ) ) {

                    CSSStyleDeclaration dec = style.declaration;

                    // if the style has the property we want

                    if ( dec != null && dec.getPropertyValue( property ) != null &&
                            !dec.getPropertyValue( property ).equals( "" ) ) {

                        return dec;
                    }
                }
            }

        }

        // since we didn't find anything, recurse up the chain

        if ( inherit ) {

            if ( elem.getParentNode() != null ) {

                Node parent = elem.getParentNode();

                //u.p("Parent node = " + parent.getNodeName());

                if ( parent instanceof Element ) {

                    return findRule( (Element)elem.getParentNode(), property, inherit );
                }
            }
        }

        //u.p("no style found at all "  + elem.getNodeName() + " prop = " + property);

        return null;
    }



    /**
     * Description of the Method
     *
     * @param selector  PARAM
     * @param node      PARAM
     * @return          Returns
     */
    protected boolean matchSelector( Selector selector, Node node ) {

        if ( selector.getSelectorType() == selector.SAC_ELEMENT_NODE_SELECTOR ) {

            return matchElement( (ElementSelector)selector, node );
        }

        if ( selector.getSelectorType() == selector.SAC_CONDITIONAL_SELECTOR ) {

            ConditionalSelector cond_sel = (ConditionalSelector)selector;

            return matchConditional( cond_sel, node );
        }

        if ( selector.getSelectorType() == selector.SAC_DESCENDANT_SELECTOR ) {

            DescendantSelector desc_sel = (DescendantSelector)selector;

            return matchDescendant( desc_sel, node );
        }

        org.xhtmlrenderer.util.u.p( "unrecognized selector type: " + selector + " node = " + node.getNodeName() );

        return false;
    }



    /**
     * Description of the Method
     *
     * @param selector  PARAM
     * @param node      PARAM
     * @return          Returns
     */
    private boolean match( String selector, Node node ) {

        try {

            CSSOMParser parser = new CSSOMParser();

            SelectorList list = parser.parseSelectors( new InputSource( new StringReader( selector ) ) );

            for ( int i = 0; i < list.getLength(); i++ ) {

                Selector sel = list.item( i );

                if ( matchSelector( sel, node ) ) {

                    return true;
                }
            }

        } catch ( Exception ex ) {

            org.xhtmlrenderer.util.u.p( ex );

        }

        return false;
    }



    /**
     * Description of the Method
     *
     * @param selector  PARAM
     * @param node      PARAM
     * @return          Returns
     */
    private boolean matchElement( ElementSelector selector, Node node ) {

        // null = any element (wildcard = *);

        if ( selector.getLocalName() == null ) {

            return true;
        }

        if ( selector.getLocalName().equals( node.getNodeName() ) ) {

            return true;
        }

        return false;
    }


    /**
     * Description of the Method
     *
     * @param selector  PARAM
     * @param node      PARAM
     * @return          Returns
     */
    private boolean matchConditional( ConditionalSelector selector, Node node ) {

        Condition cond = selector.getCondition();

        if ( cond.getConditionType() == cond.SAC_CLASS_CONDITION ) {

            return matchClassConditional( (AttributeCondition)cond, selector.getSimpleSelector(), node );
        }

        if ( cond.getConditionType() == cond.SAC_ID_CONDITION ) {

            return matchIDConditional( (AttributeCondition)cond, selector.getSimpleSelector(), node );
        }

        return false;
    }


    /**
     * Description of the Method
     *
     * @param selector  PARAM
     * @param node      PARAM
     * @return          Returns
     */
    private boolean matchDescendant( DescendantSelector selector, Node node ) {

        SimpleSelector child = selector.getSimpleSelector();

        Selector parent = selector.getAncestorSelector();

        if ( matchSelector( child, node ) ) {

            //if(matchElement((ElementSelector)child, node)) {

            Node current_node = node;

            while ( true ) {

                Node parent_node = current_node.getParentNode();

                if ( parent_node == null ) {

                    return false;
                }

                if ( matchSelector( parent, parent_node ) ) {

                    return true;
                }

                current_node = parent_node;

            }

        }

        return false;
    }


    /**
     * Description of the Method
     *
     * @param cond  PARAM
     * @param sel   PARAM
     * @param node  PARAM
     * @return      Returns
     */
    private boolean matchClassConditional( AttributeCondition cond, SimpleSelector sel, Node node ) {

        // if it's an element

        if ( !( node.getNodeType() == node.ELEMENT_NODE ) ) {

            return false;
        }

        Element elem = (Element)node;

        // if it has a class attribute

        if ( !elem.hasAttribute( "class" ) ) {

            return false;
        }

        // if the class attribute matches the condition's class

        if ( !elem.getAttribute( "class" ).equals( cond.getValue() ) ) {

            return false;
        }

        // if local name = null then it's the 'any' selector (*)

        // so it matches

        ElementSelector es = (ElementSelector)sel;

        if ( es.getLocalName() == null ) {

            return true;
        }

        if ( !( (ElementSelector)sel ).getLocalName().equals( node.getNodeName() ) ) {

            return false;
        }

        return true;
    }


    /**
     * Description of the Method
     *
     * @param cond  PARAM
     * @param sel   PARAM
     * @param node  PARAM
     * @return      Returns
     */
    private boolean matchIDConditional( AttributeCondition cond, SimpleSelector sel, Node node ) {

        // if it's an element

        if ( !( node.getNodeType() == node.ELEMENT_NODE ) ) {
            return false;
        }

        Element elem = (Element)node;

        // if it has a id attribute

        if ( !elem.hasAttribute( "id" ) ) {
            return false;
        }

        // if the id attribute matches the condition's id

        if ( !elem.getAttribute( "id" ).equals( cond.getValue() ) ) {
            return false;
        }

        // if the node names match up

        ElementSelector es = (ElementSelector)sel;

        // if local name = null then it's the 'any' selector (*)

        // so it matches

        if ( es.getLocalName() == null ) {

            return true;
        }

        if ( !( (ElementSelector)sel ).getLocalName().equals( node.getNodeName() ) ) {
            return false;
        }

        return true;
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.3  2004/10/23 13:03:45  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */

