/*
 * {{{ header & license
 * DefaultCSSPrimitiveValue.java
 * Copyright (c) 2004, 2005 Patrick Wright
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
package org.xhtmlrenderer.css.impl;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.*;
import org.xhtmlrenderer.css.constants.ValueConstants;


/**
 * A simple implementation of a CSSValue instance. Can be used for CSS initial
 * property values which are not derived from a sheet but which need to act as
 * if they are.
 *
 * @author Patrick Wright
 */
public class DefaultCSSPrimitiveValue implements CSSPrimitiveValue {
    /** */
    private String _stringValue;
    /** */
    private short _cssValueType;
    /** */
    private short _primitiveType;
    /** */
    private _RGBColor _asColor;

    /**
     * Creates a new instance of DefaultCSSValue
     *
     * @param value PARAM
     */
    public DefaultCSSPrimitiveValue(String value) {
        this._stringValue = value;
        this._cssValueType = CSS_PRIMITIVE_VALUE;
        this._primitiveType = ValueConstants.guessType(value);
    }

    /**
     * Changes the text assigned to this value
     *
     * @param cssText The new cssText value
     * @throws DOMException Throws
     */
    public void setCssText(String cssText)
            throws DOMException {
        _stringValue = cssText;
    }

    /**
     * as CSSPrimitiveValue.setFloatValue()
     *
     * @param unitType   The new floatValue value
     * @param floatValue The new floatValue value
     * @throws DOMException Throws
     */
    public void setFloatValue(short unitType, float floatValue)
            throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not a Float value.");
    }

    /**
     * as CSSPrimitiveValue.setStringValue()
     *
     * @param stringType  The new stringValue value
     * @param stringValue The new stringValue value
     * @throws DOMException Throws
     */
    public void setStringValue(short stringType, String stringValue)
            throws DOMException {
        if (stringType != CSS_STRING) {
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Can only set to CSS_STRING.");
        }

        _stringValue = stringValue;
    }

    /**
     * as same method on CSSPrimitiveValue()
     *
     * @return The cssText value
     */
    public String getCssText() {
        return _stringValue;
    }

    /**
     * as CSSValue.getCssValueType()
     *
     * @return The cssValueType value
     */
    public short getCssValueType() {
        return _cssValueType;
    }

    /**
     * as same method on CSSPrimitiveValue()
     *
     * @return The counterValue value
     * @throws DOMException Throws
     */
    public Counter getCounterValue()
            throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not a Counter value.");
    }

    /**
     * as same method on CSSPrimitiveValue()
     *
     * @param unitType PARAM
     * @return The floatValue value
     * @throws DOMException Throws
     */
    public float getFloatValue(short unitType)
            throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not a Float value.");
    }

    /**
     * as same method on CSSPrimitiveValue()
     *
     * @return The primitiveType value
     */
    public short getPrimitiveType() {
        return _primitiveType;
    }

    /**
     * as same method on CSSPrimitiveValue()
     *
     * @return The rGBColorValue value
     * @throws DOMException Throws
     */
    public RGBColor getRGBColorValue()
            throws DOMException {
        if (_stringValue.startsWith("#")) {
            if (_asColor == null) {
                _asColor = new _RGBColor(_stringValue);
            }
            return _asColor;
        } else if (_stringValue.startsWith("rgb(")) {
            String core = _stringValue.substring(4, _stringValue.length() - 1);
            String[] triple = core.split(",");
            if (triple.length != 3) {
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not an RGBColor value.");
            } else {
                _asColor = new _RGBColor(Integer.parseInt(triple[0].trim()),
                        Integer.parseInt(triple[1].trim()),
                        Integer.parseInt(triple[2].trim()));
                return _asColor;
            }
        } else {
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not an RGBColor value.");
        }
    }

    /**
     * as same method on CSSPrimitiveValue()
     *
     * @return The rectValue value
     * @throws DOMException Throws
     */
    public Rect getRectValue()
            throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not a Rect value.");
    }

    /**
     * as same method on CSSPrimitiveValue()
     *
     * @return The stringValue value
     * @throws DOMException Throws
     */
    public String getStringValue()
            throws DOMException {
        return _stringValue;
    }

    /**
     * Description of the Class
     *
     * @author Patrick Wright
     */
    static class _RGBColor implements RGBColor {
        /** */
        ColorValue redCV, greenCV, blueCV;

        /**
         * Constructor for the _RGBColor object
         *
         * @param hex PARAM
         */
        _RGBColor(String hex) {
            int red = Integer.parseInt(hex.substring(1, 3), 16);
            redCV = new ColorValue(red);
            int green = Integer.parseInt(hex.substring(3, 5), 16);
            greenCV = new ColorValue(green);
            int blue = Integer.parseInt(hex.substring(5, 7), 16);
            blueCV = new ColorValue(blue);
        }

        _RGBColor(int red, int green, int blue) {
            redCV = new ColorValue(red);
            greenCV = new ColorValue(green);
            blueCV = new ColorValue(blue);
        }

        /**
         * Gets the blue attribute of the _RGBColor object
         *
         * @return The blue value
         */
        public CSSPrimitiveValue getBlue() {
            return blueCV;
        }

        /**
         * Gets the green attribute of the _RGBColor object
         *
         * @return The green value
         */
        public CSSPrimitiveValue getGreen() {
            return greenCV;
        }

        /**
         * Gets the red attribute of the _RGBColor object
         *
         * @return The red value
         */
        public CSSPrimitiveValue getRed() {
            return redCV;
        }

        /**
         * Description of the Class
         *
         * @author Patrick Wright
         */
        class ColorValue implements CSSPrimitiveValue {
            /** */
            float _color;

            /**
             * Constructor for the ColorValue object
             *
             * @param color PARAM
             */
            ColorValue(int color) {
                this._color = new Float(color).floatValue();
            }

            /**
             * Sets the cssText attribute of the ColorValue object
             *
             * @param cssText The new cssText value
             * @throws DOMException Throws
             */
            public void setCssText(String cssText)
                    throws DOMException {
                throw new DOMException(DOMException.INVALID_ACCESS_ERR, "This is a color");
            }

            /**
             * Sets the floatValue attribute of the ColorValue object
             *
             * @param unitType   The new floatValue value
             * @param floatValue The new floatValue value
             * @throws DOMException Throws
             */
            public void setFloatValue(short unitType, float floatValue)
                    throws DOMException {
                throw new DOMException(DOMException.INVALID_ACCESS_ERR, "This is a color");
            }

            /**
             * Sets the stringValue attribute of the ColorValue object
             *
             * @param stringType  The new stringValue value
             * @param stringValue The new stringValue value
             * @throws DOMException Throws
             */
            public void setStringValue(short stringType, String stringValue)
                    throws DOMException {
                throw new DOMException(DOMException.INVALID_ACCESS_ERR, "This is a color");
            }

            /**
             * Gets the counterValue attribute of the ColorValue object
             *
             * @return The counterValue value
             * @throws DOMException Throws
             */
            public Counter getCounterValue()
                    throws DOMException {
                throw new DOMException(DOMException.INVALID_ACCESS_ERR, "This is a color");
            }

            /**
             * Gets the cssText attribute of the ColorValue object
             *
             * @return The cssText value
             */
            public String getCssText() {
                return "" + _color;
            }

            /**
             * Gets the cssValueType attribute of the ColorValue object
             *
             * @return The cssValueType value
             */
            public short getCssValueType() {
                return CSSValue.CSS_PRIMITIVE_VALUE;
            }

            /**
             * Gets the floatValue attribute of the ColorValue object
             *
             * @param unitType PARAM
             * @return The floatValue value
             * @throws DOMException Throws
             */
            public float getFloatValue(short unitType)
                    throws DOMException {
                return _color;
            }

            /**
             * Gets the primitiveType attribute of the ColorValue object
             *
             * @return The primitiveType value
             */
            public short getPrimitiveType() {
                return CSS_NUMBER;
            }

            /**
             * Gets the rGBColorValue attribute of the ColorValue object
             *
             * @return The rGBColorValue value
             * @throws DOMException Throws
             */
            public RGBColor getRGBColorValue()
                    throws DOMException {
                throw new DOMException(DOMException.INVALID_ACCESS_ERR, "This is a color");
            }

            /**
             * Gets the rectValue attribute of the ColorValue object
             *
             * @return The rectValue value
             * @throws DOMException Throws
             */
            public Rect getRectValue()
                    throws DOMException {
                throw new DOMException(DOMException.INVALID_ACCESS_ERR, "This is a color");
            }

            /**
             * Gets the stringValue attribute of the ColorValue object
             *
             * @return The stringValue value
             * @throws DOMException Throws
             */
            public String getStringValue()
                    throws DOMException {
                return "" + _color;
            }
        }
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.7  2005/10/25 15:38:27  pdoubleya
 * Moved guessType() to ValueConstants, applied fix to method suggested by Chris Oliver, to avoid exception-based catch.
 *
 * Revision 1.6  2005/06/16 07:24:45  tobega
 * Fixed background image bug.
 * Caching images in browser.
 * Enhanced LinkListener.
 * Some house-cleaning, playing with Idea's code inspection utility.
 *
 * Revision 1.5  2005/06/03 23:06:21  tobega
 * Now uses value of "color" as initial value for "border-color" and rgb-triples are supported
 *
 * Revision 1.4  2005/01/29 20:22:20  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.3  2004/12/08 00:42:29  tobega
 * More cleaning of use of Node, more preparation for Content-based inline generation. Also fixed 2 irritating bugs!
 *
 * Revision 1.2  2004/10/23 13:21:14  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

