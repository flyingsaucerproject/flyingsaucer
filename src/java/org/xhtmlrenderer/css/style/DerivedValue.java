/*
 * DerivedValue.java
 * Copyright (c) 2004, 2005 Patrick Wright, Torbj�rn Gannholm
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
package org.xhtmlrenderer.css.style;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.constants.Idents;
import org.xhtmlrenderer.css.constants.ValueConstants;
import org.xhtmlrenderer.css.util.ConversionUtil;
import org.xhtmlrenderer.extend.RenderingContext;
import org.xhtmlrenderer.layout.FontUtil;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.XRRuntimeException;

import java.awt.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A primitive value assigned to an {@link DerivedProperty}. <code>DerivedValue</code>
 * allows for easy type conversions, relative value derivation, etc. The class
 * is intended to "wrap" a {@link CSSValue} from a SAC CSS parser. Note that not
 * all type conversions make sense, and that some won't make sense until
 * relative values are resolved.
 *
 * @author Patrick Wright
 */

/*
 * NOTE:
 * DerivedValue
 * Fixed value (pixel, ident)
 * Convertible (inch, cm)
 * Proportional
 * to parent font (em)
 * to parent size (%)
 * Can also say
 * fixed? pixel value can be determined on instantiation
 * proportional? generally need to wait until requested
 */
// NOTE: we distinguish simply between two types of values: absolute and proportional
// absolute are values in pixels, or which can be converted to pixels using a fixed
public class DerivedValue {
    /**
     * Constant for CSS2 value of "inherit"
     */
    private String INHERIT = "inherit";

    /**
     * The DOM CSSValue we are given from the Parse
     */
    private CSSPrimitiveValue _domCSSPrimitiveValue;

    /** */
    private CalculatedStyle _style;

    /**
     * String array, if there is one to split from value
     */
    private String[] _stringAsArray;

    /** */
    private CSSName _cssName;

    /**
     * If background-position, and the position is absolute (on x and y axis),
     * then the <code>Point</code> for the background-position, null otherwise;
     * check <code>_bgPosIsAbsolute</code> as an alternate.
     */
    private Point _asPoint;

    /**
     * Indicates if the background position is absolute.
     */
    private boolean _bgPosIsAbsolute;

    /**
     * The x-position value of the background.
     */
    private float _bgPosXValue;

    /**
     * The type of the x-position value, i.e. it's unit.
     */
    private short _bgPosXType;

    /**
     * The y-position value of the background.
     */
    private float _bgPosYValue;

    /**
     * The type of the y-position value, i.e. it's unit.
     */
    private short _bgPosYType;

    /** */
    private String _lengthAsString;

    /** */
    private float _lengthAsFloat;

    /** */
    private float _absoluteLengthAsFloat;

    /** */
    private short _lengthPrimitiveType;
    /**
     * Description of the Field
     */
    private boolean _hasAbsCalculated;
    /**
     * Description of the Field
     */
    private Color _color;
    /**
     * Description of the Field
     */
    private IdentValue _identVal;

    /**
     * Description of the Field
     */
    private boolean _isTransparent;
    /**
     * Description of the Field
     */
    private boolean _transparentChecked;

    /**
     * A regex Pattern for CSSLength. Groups are the number portion, and the
     * suffix; if there is a match <code>matcher.group(0)</code> returns the
     * input string, <code>group(1)</code> returns the number (may be a float),
     * and <code>group(2)</code> returns the suffix. Suffix is optional in the
     * pattern, so check if <code>group(2)</code> is null before using.
     */
    private final static Pattern CSS_LENGTH_PATTERN = Pattern.compile("(-?\\d{1,10}(\\.?\\d{0,10})?)((em)|(ex)|(px)|(%)|(in)|(cm)|(mm)|(pt)|(pc))?");

    /** */
    private final static Color COLOR_TRANSPARENT = new Color(0, 0, 0, 0);

    /** Description of the Field */
    //private final float MM__PER__PX;
    /**
     * Description of the Field
     */
    private final static int MM__PER__CM = 10;
    /**
     * Description of the Field
     */
    private final static float CM__PER__IN = 2.54F;
    /**
     * Description of the Field
     */
    private final static float PT__PER__IN = 1f / 72f;
    /**
     * Description of the Field
     */
    private final static float PC__PER__PT = 12;

    /**
     * @param cssName
     * @param primitive
     * @param style     to which this value belongs
     */
    public DerivedValue(CSSName cssName, CSSPrimitiveValue primitive, CalculatedStyle style) {
        _cssName = cssName;
        _domCSSPrimitiveValue = primitive;
        String org = _domCSSPrimitiveValue.getCssText();
        primitive.setCssText(Idents.convertIdent(cssName, org));
        if (primitive.getCssText() == null) {
            throw new XRRuntimeException("CSSValue for '" + cssName + "' is null after " +
                    "resolving CSS identifier for value '" + org + "'");
        }
        _style = style;
        // We must calculate this based on the current DPI
        //tobe: resolve it through RenderingContext MM__PER__PX = ( CM__PER__IN * MM__PER__CM ) / style.getContext().getCtx().getDPI();

        try {
            if (_cssName == CSSName.BACKGROUND_POSITION) {
                pullPointValuesForBGPos(primitive);
            } else {
                if (Idents.looksLikeALength(primitive.getCssText()) && !(_cssName == CSSName.FONT_WEIGHT)) {
                    // split out the length (as string), as float, as the primitive type
                    pullLengthValueParts(primitive);
                }
            }
        } catch (Exception ex) {
            if (_style.getParent() != null) {
                _style.getParent().dumpProperties();
            }
            throw new XRRuntimeException("For " + cssName + ": '" + org + "', failed to instantiate DerivedValue. " + ex.getMessage());
        }
    }

    /**
     * Description of the Method
     *
     * @return Returns
     */
    public boolean hasAbsoluteUnit() {
        //TODO:check list of names. Also, method may be unnecessary (tobe)
        return (_cssName == CSSName.BACKGROUND_POSITION ||
                ValueConstants.isAbsoluteUnit(_domCSSPrimitiveValue));
    }

    /**
     * Deep copy operation. However, any contained SAC instances are not
     * deep-copied. This can be used for an inherited value where the new value
     * might be calculated differently from the original. For example, if the
     * <copy>DerivedValue</copy> you are copying is a height of 1.3em, the
     * inherited value will be calculated based on its parent element, not on
     * the original parent element. This method lets you get a copy of the
     * original which calculates its own length value relative to its new
     * parent.
     *
     * @return A clone of this <copy>DerivedValue</copy> , pointing to the
     *         same SAC {@link CSSValue} as the original.
     */
    public DerivedValue copyOf() {
        DerivedValue nv = new DerivedValue(_cssName, _domCSSPrimitiveValue, _style);
        return nv;
    }

    /**
     * value as a string...same as getStringValue() but kept for parallel with
     * other as <type>... methods
     *
     * @return Returns
     */
    public String asString() {
        return getStringValue();
    }


    /**
     * Returns the value as assigned, split into a string array on comma.
     *
     * @return Returns
     */
    public String[] asStringArray() {
        if (_stringAsArray == null) {
            String str = getStringValue();
            _stringAsArray = (str == null ? new String[0] : str.split(","));
        }
        return _stringAsArray;
    }


    /**
     * The value as a CSSPrimitiveValue; changes to the CSSPrimitiveValue are
     * not tracked. Any changes to the properties should be made through the
     * DerivedProperty and DerivedValue classes.
     *
     * @return Returns
     */
    public CSSPrimitiveValue cssValue() {
        return _domCSSPrimitiveValue;
    }


    /**
     * See interface.
     *
     * @return See desc.
     */
    public boolean forcedInherit() {
        return _domCSSPrimitiveValue.getCssText().indexOf(INHERIT) >= 0;
    }

    /**
     * Returns the value as a Color, if it is a color.
     *
     * @return The rGBColorValue value
     */
    public Color asColor() {
        _color = null;

        if (_color == null) {
            String str = _domCSSPrimitiveValue.getCssText();
            try {

                if (isTransparent(str)) {
                    _color = COLOR_TRANSPARENT;
                } else if (_domCSSPrimitiveValue.getPrimitiveType() == CSSPrimitiveValue.CSS_RGBCOLOR) {
                    _color = ConversionUtil.rgbToColor(_domCSSPrimitiveValue.getRGBColorValue());
                } else {
                    _color = Color.decode(str);
                }
            } catch (Exception ex) {
                throw new XRRuntimeException("Could not return '" + _cssName + "' in a DerivedValue as a Color (value '" + str + "')." + ex.getMessage());
            }
        }
        return _color;
    }

    /**
     * @param parentWidth
     * @param parentHeight
     * @param ctx
     * @return
     */
    public Point asPoint(float parentWidth, float parentHeight, RenderingContext ctx) {
        Point pt = null;
        if (_bgPosIsAbsolute) {
            // It's an absolute value, so only calculate it once
            if (_asPoint == null) {
                _asPoint = new Point();
                float xF = calcFloatProportionalValue(_bgPosXValue, _bgPosXType, parentWidth, ctx);
                float yF = calcFloatProportionalValue(_bgPosYValue, _bgPosYType, parentHeight, ctx);
                _asPoint.setLocation(xF, yF);
            }
            pt = _asPoint;
        } else {
            pt = new Point();
            float xF = calcFloatProportionalValue(_bgPosXValue, _bgPosXType, parentWidth, ctx);
            float yF = calcFloatProportionalValue(_bgPosYValue, _bgPosYType, parentHeight, ctx);
            pt.setLocation(xF, yF);
        }
        // System.out.println("[" + this.hashCode() + "]   background-position (absolute: " + _bgPosIsAbsolute + ") " + " (" + _domCSSPrimitiveValue.getCssText() + ") x:" + pt.getX() + " y:" + pt.getY());
        return pt;
    }

    /**
     * Description of the Method
     *
     * @return Returns
     */
    public IdentValue asIdentValue() {
        if (_identVal == null && _lengthAsString == null) {
            _identVal = IdentValue.getByIdentString(asString());
        }
        return _identVal;
    }

    /**
     * @return
     */
    public boolean isIdentifier() {
        return (_domCSSPrimitiveValue.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT);
    }

    /**
     * Computes a relative unit (e.g. percentage) as an absolute value, using
     * the input value. Used for such properties whose parent value cannot be
     * known before layout/render
     *
     * @param parentWidth The value that this should be relative to.
     * @param ctx
     * @return the absolute value or computed absolute value
     */
    public float getFloatProportionalWidth(float parentWidth, RenderingContext ctx) {
        return calcFloatProportionalValue(_lengthAsFloat, _lengthPrimitiveType, parentWidth, ctx);
    }

    /**
     * Computes a relative unit (e.g. percentage) as an absolute value, using
     * the input value. Used for such properties whose parent value cannot be
     * known before layout/render
     *
     * @param parentHeight The value that this should be relative to.
     * @param ctx
     * @return the absolute value or computed absolute value
     */
    public float getFloatProportionalHeight(float parentHeight, RenderingContext ctx) {
        return calcFloatProportionalValue(_lengthAsFloat, _lengthPrimitiveType, parentHeight, ctx);
    }

    /**
     * See interface.
     *
     * @return Returns
     */
    public String getStringValue() {
        try {
            switch (_domCSSPrimitiveValue.getPrimitiveType()) {
                case CSSPrimitiveValue.CSS_IDENT:
                case CSSPrimitiveValue.CSS_STRING:
                case CSSPrimitiveValue.CSS_URI:
                case CSSPrimitiveValue.CSS_ATTR:
                    return _domCSSPrimitiveValue.getStringValue();
                default:
                    return _domCSSPrimitiveValue.getCssText();
            }
        } catch (Throwable thr) {
            // CLEAN
            XRLog.general(Level.WARNING, "exception: " + thr);
            XRLog.general(Level.WARNING, "value = " + _domCSSPrimitiveValue);
            throw (new Error(thr));
        }
    }

    /**
     * Gets the ident attribute of the DerivedValue object
     *
     * @param val PARAM
     * @return The ident value
     */
    public boolean isIdent(IdentValue val) {
        IdentValue compareTo = asIdentValue();
        return compareTo != null && compareTo == val;
    }

    /**
     * Given the {@link CSSValue}, which contains a string holding a CSSLength,
     * pull out the numeric portion and the type portion separately; stored as
     * member fields in this class. We use a regex to do this.
     *
     * @param primitive
     */
    private void pullLengthValueParts(CSSPrimitiveValue primitive) {
        Matcher m = CSS_LENGTH_PATTERN.matcher(primitive.getCssText());
        if (m.matches()) {
            _lengthAsString = m.group(1);
            _lengthAsFloat = new Float(_lengthAsString).floatValue();
            _lengthPrimitiveType = ValueConstants.sacPrimitiveTypeForString(m.group(3));
        } else {
            throw new XRRuntimeException("Could not extract length for " + _cssName + " from " + primitive.getCssText() +
                    " using " + CSS_LENGTH_PATTERN);
        }

        if (_lengthAsString == null) {
            throw new XRRuntimeException("Could not extract length for " + _cssName + " from " + primitive.getCssText() +
                    "; is null, using " + CSS_LENGTH_PATTERN);

        }
    }

    /**
     * This method extracts the two values from the background-position
     * assignment. It tries to resolve them if both values are absolute, but if
     * proportional, this is deferred until the Point is requested. We pull
     * immediately because it's a small String operation that would be silly to
     * reproduce on each request.
     *
     * @param primitive The underlying SAC {@link CSSValue}
     */
    private void pullPointValuesForBGPos(CSSPrimitiveValue primitive) {
        String cssText = primitive.getCssText();
        String[] pos = cssText.split(" ");
        try {
            Matcher m = CSS_LENGTH_PATTERN.matcher(pos[0]);
            m.matches();
            String xAsString = m.group(1);
            _bgPosXValue = new Float(xAsString).floatValue();
            _bgPosXType = ValueConstants.sacPrimitiveTypeForString(m.group(3));

            m = CSS_LENGTH_PATTERN.matcher(pos[1]);
            m.matches();
            String yAsString = m.group(1);
            _bgPosYValue = new Float(yAsString).floatValue();
            _bgPosYType = ValueConstants.sacPrimitiveTypeForString(m.group(3));

            if (ValueConstants.isAbsoluteUnit(_bgPosXType) && ValueConstants.isAbsoluteUnit(_bgPosYType)) {
                _bgPosIsAbsolute = true;
            } else {
                _bgPosIsAbsolute = false;
            }
        } catch (Exception ex) {
            StringBuffer msg = new StringBuffer();
            msg.append("background-position: failed to convert '" + cssText + "' into a Point. ");
            msg.append("Property value (as text) was split into " + pos.length + " values for positioning. ");
            if (pos.length >= 1) {
                msg.append(" background-position x-pos is " + pos[0]);
            }
            if (pos.length == 2) {
                msg.append(" background-position y-pos is " + pos[1]);
            }
            throw new XRRuntimeException(msg.toString());
        }
    }

    /**
     * Calculates the absolute (pixel) value of a property. This is used both
     * for absolute and for proportional values for code modularity. It does not
     * use or modify member fields because it can be used for background
     * position (as Point), where the X and Y must be calculated separately; so
     * it is more or less a static function.
     *
     * @param relVal        The property value, possibly proportional, which
     *                      should be converted.
     * @param primitiveType The short type code for the CSSPrimitiveValue we
     *                      are converting.
     * @param baseValue     The width, in pixels, of the parent container; only
     *                      relevant for proportional values that are width-dependent. For
     *                      safety, always pass in the correct value in context if you have it
     *                      (e.g. don't make assumptions about what values are necessary in the
     *                      calling code.
     * @param ctx
     * @return The value, in pixels, for this property.
     */
    private float calcFloatProportionalValue(float relVal, short primitiveType, float baseValue, RenderingContext ctx) {
        float absVal = Float.MIN_VALUE;

        // NOTE: absolute datatypes (px, pt, pc, cm, etc.) are converted once and stored.
        // this could be done on instantiation, but it seems more clear to have all the calcs
        // in one place. For this reason we use the member field boolean hasAbsCalculated to
        // track if the calculation is already done.
        switch (primitiveType) {
            case CSSPrimitiveValue.CSS_PX:
                // nothing to do
                if (_hasAbsCalculated) {
                    absVal = _absoluteLengthAsFloat;
                } else {
                    absVal = relVal;
                    _absoluteLengthAsFloat = absVal;
                    _hasAbsCalculated = true;
                }
                break;
            case CSSPrimitiveValue.CSS_IN:
                if (_hasAbsCalculated) {
                    absVal = _absoluteLengthAsFloat;
                } else {
                    absVal = (((relVal * CM__PER__IN) * MM__PER__CM) / ctx.getMmPerPx());
                    _absoluteLengthAsFloat = absVal;
                    _hasAbsCalculated = true;
                }
                break;
            case CSSPrimitiveValue.CSS_CM:
                if (_hasAbsCalculated) {
                    absVal = _absoluteLengthAsFloat;
                } else {
                    absVal = ((relVal * MM__PER__CM) / ctx.getMmPerPx());
                    _absoluteLengthAsFloat = absVal;
                    _hasAbsCalculated = true;
                }
                break;
            case CSSPrimitiveValue.CSS_MM:
                if (_hasAbsCalculated) {
                    absVal = _absoluteLengthAsFloat;
                } else {
                    absVal = relVal / ctx.getMmPerPx();
                    _absoluteLengthAsFloat = absVal;
                    _hasAbsCalculated = true;
                }
                break;
            case CSSPrimitiveValue.CSS_PT:
                if (_hasAbsCalculated) {
                    absVal = _absoluteLengthAsFloat;
                } else {
                    absVal = (((relVal * PT__PER__IN) * CM__PER__IN) * MM__PER__CM) / ctx.getMmPerPx();
                    _absoluteLengthAsFloat = absVal;
                    _hasAbsCalculated = true;
                }
                break;
            case CSSPrimitiveValue.CSS_PC:
                if (_hasAbsCalculated) {
                    absVal = _absoluteLengthAsFloat;
                } else {
                    absVal = ((((relVal * PC__PER__PT) * PT__PER__IN) * CM__PER__IN) * MM__PER__CM) / ctx.getMmPerPx();
                    _absoluteLengthAsFloat = absVal;
                    _hasAbsCalculated = true;
                }
                break;
            case CSSPrimitiveValue.CSS_EMS:
                // EM is equal to font-size of element on which it is used
                // The exception is when �em� occurs in the value of
                // the �font-size� property itself, in which case it refers
                // to the calculated font size of the parent element (spec: 4.3.2)
                if (_cssName == CSSName.FONT_SIZE) {
                    absVal = relVal * _style.getParent().getFont(ctx).getSize2D();
                } else {
                    absVal = relVal * _style.getFont(ctx).getSize2D();
                }

                break;
            case CSSPrimitiveValue.CSS_EXS:
                // To convert EMS to pixels, we need the height of the lowercase 'Xx' character in the current
                // element...
                // to the font size of the parent element (spec: 4.3.2)
                if (_cssName == CSSName.FONT_SIZE) {
                    Font parentFont = _style.getParent().getFont(ctx);
                    float xHeight = FontUtil.getXHeight(ctx, parentFont);
                    xHeight = relVal * xHeight;
                    absVal = _style.getFontSizeForXHeight(ctx, xHeight);
                } else {
                    Font font = _style.getFont(ctx);
                    float xHeight = FontUtil.getXHeight(ctx, font);
                    absVal = relVal * xHeight;
                }

                break;
            case CSSPrimitiveValue.CSS_PERCENTAGE:
                // percentage depends on the property this value belongs to
                if (_cssName == CSSName.VERTICAL_ALIGN) {
                    relVal = _style.getParent().getFloatPropertyProportionalHeight(CSSName.LINE_HEIGHT, baseValue, ctx);
                } else if (_cssName == CSSName.FONT_SIZE) {
                    // same as with EM
                    baseValue = _style.getParent().getFont(ctx).getSize2D();
                }
                absVal = (relVal / 100F) * baseValue;

                break;
            default:
                // nothing to do, we only convert those listed above
                XRLog.cascade(Level.SEVERE,
                        "Asked to convert " + _cssName + " from relative to absolute, " +
                        " don't recognize the datatype " +
                        "'" + ValueConstants.stringForSACPrimitiveType(_lengthPrimitiveType) + "' "
                        + _lengthPrimitiveType + "(" + _domCSSPrimitiveValue.getCssText() + ")");
        }
        assert (new Float(absVal).intValue() > 0);

        if (_cssName == CSSName.FONT_SIZE) {
            XRLog.cascade(Level.FINEST, _cssName + ", relative= " +
                    relVal + " (" + _domCSSPrimitiveValue.getCssText() + "), absolute= "
                    + absVal);
        } else {
            XRLog.cascade(Level.FINEST, _cssName + ", relative= " +
                    relVal + " (" + _domCSSPrimitiveValue.getCssText() + "), absolute= "
                    + absVal + " using base=" + baseValue);
        }

        // round down. (CHECK: Why? Is this always appropriate? - tobe)
        double d = Math.floor((double) absVal);
        absVal = new Float(d).floatValue();
        return absVal;
    }

    /**
     * Gets the transparent attribute of the DerivedValue object
     *
     * @param str PARAM
     * @return The transparent value
     */
    private boolean isTransparent(String str) {
        if (!_transparentChecked) {
            _isTransparent = "transparent".equals(str);
            _transparentChecked = true;
        }

        return _isTransparent;
    }
}// end class

