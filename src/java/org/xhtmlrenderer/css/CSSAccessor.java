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
import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;
import org.w3c.dom.css.RGBColor;
import org.xhtmlrenderer.css.value.BorderColor;

import org.xhtmlrenderer.util.u;


/**
 * @author   empty
 */
public abstract class CSSAccessor implements StyleReference {

    /**
     * Description of the Method
     *
     * @param elem  PARAM
     * @param prop  PARAM
     * @return      Returns
     */
    public boolean hasProperty( Element elem, String prop ) {
        return hasProperty( elem, prop, true );
    }

    /**
     * Description of the Method
     *
     * @param elem     PARAM
     * @param prop     PARAM
     * @param inherit  PARAM
     * @return         Returns
     */
    public boolean hasProperty( Node elem, String prop, boolean inherit ) {
        if ( elem instanceof Element ) {
            return hasProperty( (Element)elem, prop, inherit );
        } else {
            return hasProperty( (Element)( elem.getParentNode() ), prop, inherit );
        }
    }


    /**
     * Description of the Method
     *
     * @param elem     PARAM
     * @param prop     PARAM
     * @param inherit  PARAM
     * @return         Returns
     */
    public boolean hasProperty( Element elem, String prop, boolean inherit ) {
        CSSValue val = getProperty( elem, prop, inherit );
        if ( val == null ) {
            return false;
        }
        return true;
    }

    /**
     * Gets the property attribute of the CSSAccessor object
     *
     * @param elem     PARAM
     * @param prop     PARAM
     * @param inherit  PARAM
     * @return         The property value
     */
    public abstract CSSValue getProperty( Element elem, String prop, boolean inherit );

    // easy accessors
    /**
     * Gets the floatProperty attribute of the CSSAccessor object
     *
     * @param elem          PARAM
     * @param prop          PARAM
     * @param parent_value  PARAM
     * @return              The floatProperty value
     */
    public float getFloatProperty( Element elem, String prop, float parent_value ) {
        return this.getFloatProperty( elem, prop, parent_value, true );
    }

    /**
     * Gets the floatProperty attribute of the CSSAccessor object
     *
     * @param elem          PARAM
     * @param prop          PARAM
     * @param parent_value  PARAM
     * @param inherit       PARAM
     * @return              The floatProperty value
     */
    public float getFloatProperty( Node elem, String prop, float parent_value, boolean inherit ) {
        if ( elem instanceof Element ) {
            return getFloatProperty( (Element)elem, prop, parent_value, inherit );
        } else {
            return getFloatProperty( (Element)( elem.getParentNode() ), prop, parent_value, inherit );
        }
    }

    /**
     * Gets the floatProperty attribute of the CSSAccessor object
     *
     * @param elem  PARAM
     * @param prop  PARAM
     * @return      The floatProperty value
     */
    public float getFloatProperty( Element elem, String prop ) {
        return getFloatProperty( elem, prop, true );
    }

    /**
     * Gets the stringProperty attribute of the CSSAccessor object
     *
     * @param elem  PARAM
     * @param prop  PARAM
     * @return      The stringProperty value
     */
    public String getStringProperty( Element elem, String prop ) {
        return getStringProperty( elem, prop, true );
    }

    /**
     * Gets the stringProperty attribute of the CSSAccessor object
     *
     * @param elem  PARAM
     * @param prop  PARAM
     * @return      The stringProperty value
     */
    public String getStringProperty( Node elem, String prop ) {
        return getStringProperty( elem, prop, true );
    }


    /**
     * Gets the floatProperty attribute of the CSSAccessor object
     *
     * @param elem          PARAM
     * @param prop          PARAM
     * @param parent_value  PARAM
     * @param inherit       PARAM
     * @return              The floatProperty value
     */
    public float getFloatProperty( Element elem, String prop, float parent_value, boolean inherit ) {
        CSSValue val = getProperty( elem, prop, inherit );
        //u.p("potential float value = " + val);
        if ( val == null ) {
            //u.dump_stack();
            //u.p("elem " + elem.getNodeName() + " doesn't have the property: " + prop);
            return -1;
        }

        if ( val.getCssValueType() != val.CSS_PRIMITIVE_VALUE ) {
            u.p( val + " isn't a primitive value" );
            return -1;
        }

        CSSPrimitiveValue pval = (CSSPrimitiveValue)val;
        //u.p("pval = " + pval);
        if ( pval.getPrimitiveType() == pval.CSS_PERCENTAGE ) {
            float retval = ( pval.getFloatValue( pval.CSS_PERCENTAGE ) / 100 ) * parent_value;
            //u.p("parent: " + parent_value + " %: " + pval.getFloatValue(pval.CSS_PERCENTAGE) + " return: " + retval);
            return retval;
        }
        if ( pval.getPrimitiveType() == pval.CSS_EMS ) {
            //u.p("val = " + pval);
            float retval = ( pval.getFloatValue( pval.CSS_PERCENTAGE ) ) * parent_value;
            return retval;
        }
        //u.p("returning: " + pval.getFloatValue(pval.CSS_PX));
        return pval.getFloatValue( pval.CSS_PX );
    }


    /**
     * Gets the floatProperty attribute of the CSSAccessor object
     *
     * @param elem     PARAM
     * @param prop     PARAM
     * @param inherit  PARAM
     * @return         The floatProperty value
     */
    public float getFloatProperty( Element elem, String prop, boolean inherit ) {
        //u.p("get float property " + elem.getNodeName() + " " + prop);
        CSSValue val = getProperty( elem, prop, inherit );
        //u.p("potential float value = " + val);
        if ( val == null ) {
            //u.p("elem " + elem.getNodeName() + " doesn't have the property: " + prop);
            return -1;
        }
        if ( val.getCssValueType() != val.CSS_PRIMITIVE_VALUE ) {
            u.p( val + " isn't a primitive value" );
            return -1;
        }
        CSSPrimitiveValue pval = (CSSPrimitiveValue)val;
        //u.p("returning: " + pval.getFloatValue(pval.CSS_PX));
        return pval.getFloatValue( pval.CSS_PX );
    }


    /**
     * Gets the stringProperty attribute of the CSSAccessor object
     *
     * @param elem     PARAM
     * @param prop     PARAM
     * @param inherit  PARAM
     * @return         The stringProperty value
     */
    public String getStringProperty( Node elem, String prop, boolean inherit ) {
        if ( elem instanceof Element ) {
            return getStringProperty( (Element)elem, prop, true );
        } else {
            return getStringProperty( (Element)( elem.getParentNode() ), prop, inherit );
        }
    }

    /**
     * Gets the stringProperty attribute of the CSSAccessor object
     *
     * @param elem     PARAM
     * @param prop     PARAM
     * @param inherit  PARAM
     * @return         The stringProperty value
     */
    public String getStringProperty( Element elem, String prop, boolean inherit ) {
        CSSValue val = getProperty( elem, prop, inherit );
        if ( val == null ) {
            return null;
        }
        if ( val.getCssValueType() != val.CSS_PRIMITIVE_VALUE ) {
            u.p( val + " isn't a primitive value" );
            return null;
        }
        CSSPrimitiveValue pval = (CSSPrimitiveValue)val;
        return pval.getStringValue();
    }

    /**
     * Gets the floatPairProperty attribute of the CSSAccessor object
     *
     * @param elem     PARAM
     * @param prop     PARAM
     * @param inherit  PARAM
     * @return         The floatPairProperty value
     */
    public Point getFloatPairProperty( Element elem, String prop, boolean inherit ) {
        CSSValue val = getProperty( elem, prop, inherit );
        if ( val == null ) {
            return null;
        }
        if ( val.getCssValueType() == val.CSS_VALUE_LIST ) {
            CSSValueList vl = (CSSValueList)val;
            CSSPrimitiveValue pval = (CSSPrimitiveValue)val;
            Point pt = new Point();
            pt.setLocation(
                    ( (CSSPrimitiveValue)vl.item( 0 ) ).getFloatValue( pval.CSS_PERCENTAGE ),
                    ( (CSSPrimitiveValue)vl.item( 1 ) ).getFloatValue( pval.CSS_PERCENTAGE )
                     );
            return pt;
        }
        return null;
    }

    /**
     * Gets the stringArrayProperty attribute of the CSSAccessor object
     *
     * @param elem  PARAM
     * @param prop  PARAM
     * @return      The stringArrayProperty value
     */
    public String[] getStringArrayProperty( Element elem, String prop ) {
        CSSValue val = getProperty( elem, prop, true );
        if ( val == null ) {
            u.p( "not array found" );
            return null;
        }
        ArrayList al = new ArrayList();
        if ( val.getCssValueType() == val.CSS_VALUE_LIST ) {
            //u.p("got a list");
            CSSValueList vl = (CSSValueList)val;
            for ( int i = 0; i < vl.getLength(); i++ ) {
                al.add( ( (CSSPrimitiveValue)vl.item( i ) ).getStringValue() );
            }
        } else {
            //u.p("it wasn't a value list!: " + val);
            al.add( ( (CSSPrimitiveValue)val ).getStringValue() );
        }
        return u.list_to_strings( al );
    }

    /*
     * ========== Color stuff =============
     */
    /**
     * Gets the backgroundColor attribute of the CSSAccessor object
     *
     * @param elem  PARAM
     * @return      The backgroundColor value
     */
    public Color getBackgroundColor( Element elem ) {
        CSSValue val = getProperty( elem, "background-color", false );
        //u.p("background for: " + elem.getNodeName() + " =  " + val);
        if ( val == null ) {
            return null;
        }
        return rgbToColor( ( (CSSPrimitiveValue)val ).getRGBColorValue() );
    }

    // CLEAN: BorderColor change...
    /**
     * Gets the borderColor attribute of the CSSAccessor object
     *
     * @param elem  PARAM
     * @return      The borderColor value
     */
    public BorderColor getBorderColor( Element elem ) {
        CSSValue val = getProperty( elem, "border-color", true );
        if ( val == null ) {
            return null;
        }
        BorderColor bc = new BorderColor();
        Color c = rgbToColor( ( (CSSPrimitiveValue)val ).getRGBColorValue() );
        bc.topColor = c;
        bc.rightColor = c;
        bc.bottomColor = c;
        bc.leftColor = c;
        return bc;
    }

    /**
     * Gets the color attribute of the CSSAccessor object
     *
     * @param elem  PARAM
     * @return      The color value
     */
    public Color getColor( Element elem ) {
        return getColor( elem, true );
    }

    /**
     * Gets the color attribute of the CSSAccessor object
     *
     * @param elem     PARAM
     * @param inherit  PARAM
     * @return         The color value
     */
    public Color getColor( Element elem, boolean inherit ) {
        //u.p("CSSBank.getColor("+elem.getNodeName() + ")");
        CSSValue val = getProperty( elem, "color", inherit );

        if ( val == null ) {
            u.p( "null returned on: " + elem.getNodeName() );
            u.p( "property color" );
            return null;
        }
        if ( val.getCssValueType() == val.CSS_PRIMITIVE_VALUE ) {
            CSSPrimitiveValue pval = (CSSPrimitiveValue)val;
            if ( pval.getPrimitiveType() == pval.CSS_RGBCOLOR ) {
                RGBColor rgbcol = pval.getRGBColorValue();
                //u.p("returning color: " + rgbToColor(rgbcol));
                return rgbToColor( rgbcol );
            }
        }
        return null;
    }


    /*
     * ======= margins, border, padding ==========
     */
    /**
     * Gets the borderWidth attribute of the CSSAccessor object
     *
     * @param elem  PARAM
     * @return      The borderWidth value
     */
    public Border getBorderWidth( Element elem ) {
        float top = getFloatProperty( elem, "border-top-width" );
        float bottom = getFloatProperty( elem, "border-bottom-width" );
        float left = getFloatProperty( elem, "border-left-width" );
        //u.p("left w/o inherit = " + left + " for elem: " + elem);
        float right = getFloatProperty( elem, "border-right-width" );
        Border border = new Border();
        border.top = (int)top;
        border.bottom = (int)bottom;
        border.left = (int)left;
        border.right = (int)right;
        return border;
    }

    /**
     * Gets the paddingWidth attribute of the CSSAccessor object
     *
     * @param elem  PARAM
     * @return      The paddingWidth value
     */
    public Border getPaddingWidth( Element elem ) {
        float top = getFloatProperty( elem, "padding-top" );
        float bottom = getFloatProperty( elem, "padding-bottom" );
        float left = getFloatProperty( elem, "padding-left" );
        float right = getFloatProperty( elem, "padding-right" );
        Border border = new Border();
        border.top = (int)top;
        border.bottom = (int)bottom;
        border.left = (int)left;
        border.right = (int)right;
        return border;
    }

    /**
     * Gets the marginWidth attribute of the CSSAccessor object
     *
     * @param elem  PARAM
     * @return      The marginWidth value
     */
    public Border getMarginWidth( Element elem ) {
        float top = getFloatProperty( elem, "margin-top" );
        float bottom = getFloatProperty( elem, "margin-bottom" );
        float left = getFloatProperty( elem, "margin-left" );
        float right = getFloatProperty( elem, "margin-right" );
        Border border = new Border();
        border.top = (int)top;
        border.bottom = (int)bottom;
        border.left = (int)left;
        border.right = (int)right;
        return border;
    }

    /**
     * Description of the Method
     *
     * @param rgbcol  PARAM
     * @return        Returns
     */
    private Color rgbToColor( RGBColor rgbcol ) {
        return new java.awt.Color( rgbcol.getRed().getFloatValue( CSSPrimitiveValue.CSS_NUMBER ) / 255f,
                rgbcol.getGreen().getFloatValue( CSSPrimitiveValue.CSS_NUMBER ) / 255f,
                rgbcol.getBlue().getFloatValue( CSSPrimitiveValue.CSS_NUMBER ) / 255f );
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.5  2004/10/28 13:39:46  joshy
 * removed dead code
 *
 * Revision 1.4  2004/10/23 13:03:45  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */

