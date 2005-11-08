package org.xhtmlrenderer.css.style.derived;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.ValueConstants;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.DerivedValue;
import org.xhtmlrenderer.css.style.FSDerivedValue;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.XRRuntimeException;

import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Oct 17, 2005
 * Time: 2:09:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class LengthValue extends DerivedValue {
    /**
     * A regex Pattern for CSSLength. Groups are the number portion, and the
     * suffix; if there is a match <code>matcher.group(0)</code> returns the
     * input string, <code>group(1)</code> returns the number (may be a float),
     * and <code>group(2)</code> returns the suffix. Suffix is optional in the
     * pattern, so check if <code>group(2)</code> is null before using.
     */
    private final static Pattern CSS_LENGTH_PATTERN = Pattern.compile("(-?\\d{1,10}(\\.?\\d{0,10})?)((em)|(ex)|(px)|(%)|(in)|(cm)|(mm)|(pt)|(pc))?");

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
     * The specified length value, as a float; pulled from the CSS text
     */
    private float _lengthAsFloat;

    /**
     * The specified primitive SAC data type given for this length, from the CSS text
     */
    private short _lengthPrimitiveType;

    protected LengthValue() {
        super();
    }

    public LengthValue(CalculatedStyle style,
                       CSSName name,
                       short cssSACUnitType,
                       String cssText,
                       String cssStringValue) {
        super(style, name, cssSACUnitType, cssText, cssStringValue);
        pullLengthValueParts(name);
    }

    public float asFloat() {
        return _lengthAsFloat;
    }

    public FSDerivedValue copyOf(CSSName cssName) {
        return new LengthValue(getStyle(), cssName, getCssSacUnitType(), getStringValue(), getStringValue());
    }

    /**
     * Computes a relative unit (e.g. percentage) as an absolute value, using
     * the input value. Used for such properties whose parent value cannot be
     * known before layout/render
     *
     * @param cssName   Name of the property
     * @param baseValue
     * @param ctx
     * @return the absolute value or computed absolute value
     */
    public float getFloatProportionalTo(CSSName cssName,
                                        float baseValue,
                                        CssContext ctx) {
        return calcFloatProportionalValue(getStyle(),
                cssName,
                getStringValue(),
                _lengthAsFloat,
                _lengthPrimitiveType,
                baseValue,
                ctx);
    }

    protected static Matcher getLengthMatcher(String len) {
        return CSS_LENGTH_PATTERN.matcher(len);
    }

    public boolean hasAbsoluteUnit() {
        return ValueConstants.isAbsoluteUnit(getCssSacUnitType());
    }

    /**
     * Given the {@link org.w3c.dom.css.CSSValue}, which contains a string holding a CSSLength,
     * pull out the numeric portion and the type portion separately; stored as
     * member fields in this class. We use a regex to do this.
     */
    private void pullLengthValueParts(CSSName cssName) {
        Matcher m = CSS_LENGTH_PATTERN.matcher(getStringValue());
        String lengthAsString = null;
        if (m.matches()) {
            lengthAsString = m.group(1);
            _lengthAsFloat = new Float(lengthAsString).floatValue();
            _lengthPrimitiveType = ValueConstants.sacPrimitiveTypeForString(m.group(3));
        } else {
            throw new XRRuntimeException("Could not extract length for " + cssName +
                    " from " + getStringValue() +
                    " using " + CSS_LENGTH_PATTERN);
        }

        if (lengthAsString == null) {
            throw new XRRuntimeException("Could not extract length for " + cssName +
                    " from " + getStringValue() +
                    "; is null, using " + CSS_LENGTH_PATTERN);
        }
    }


    protected static float calcFloatProportionalValue(CalculatedStyle style,
                                                      CSSName cssName,
                                                      String stringValue,
                                                      float relVal,
                                                      short primitiveType,
                                                      float baseValue,
                                                      CssContext ctx) {

        float absVal = Float.MIN_VALUE;

        // NOTE: we used to cache absolute values, but have removed that to see if it
        // really makes a difference, since the calcs are so simple. In any case, for DPI-relative
        // values we shouldn't be caching, unless we also check if the DPI is changed, which
        // would seem to obviate the advantage of caching anyway.
        switch (primitiveType) {
            case CSSPrimitiveValue.CSS_PX:
                absVal = relVal;
                break;
            case CSSPrimitiveValue.CSS_IN:
                absVal = (((relVal * CM__PER__IN) * MM__PER__CM) / ctx.getMmPerPx());
                break;
            case CSSPrimitiveValue.CSS_CM:
                absVal = ((relVal * MM__PER__CM) / ctx.getMmPerPx());
                break;
            case CSSPrimitiveValue.CSS_MM:
                absVal = relVal / ctx.getMmPerPx();
                break;
            case CSSPrimitiveValue.CSS_PT:
                absVal = (((relVal * PT__PER__IN) * CM__PER__IN) * MM__PER__CM) / ctx.getMmPerPx();
                break;
            case CSSPrimitiveValue.CSS_PC:
                absVal = ((((relVal * PC__PER__PT) * PT__PER__IN) * CM__PER__IN) * MM__PER__CM) / ctx.getMmPerPx();
                break;
            case CSSPrimitiveValue.CSS_EMS:
                // EM is equal to font-size of element on which it is used
                // The exception is when ?em? occurs in the value of
                // the ?font-size? property itself, in which case it refers
                // to the calculated font size of the parent element
                // http://www.w3.org/TR/CSS21/fonts.html#font-size-props
                if (cssName == CSSName.FONT_SIZE) {
                    FontSpecification parentFont = style.getParent().getFont(ctx);
                    absVal = relVal * ctx.getFontSize2D(parentFont);
                } else {
                    absVal = relVal * ctx.getFontSize2D(style.getFont(ctx));
                }

                break;
            case CSSPrimitiveValue.CSS_EXS:
                // To convert EMS to pixels, we need the height of the lowercase 'Xx' character in the current
                // element...
                // to the font size of the parent element (spec: 4.3.2)
                if (cssName == CSSName.FONT_SIZE) {
                    FontSpecification parentFont = style.getParent().getFont(ctx);
                    float xHeight = ctx.getXHeight(parentFont);
                    xHeight = relVal * xHeight;
                    absVal = getFontSizeForXHeight(style, ctx, xHeight);
                } else {
                    FontSpecification font = style.getFont(ctx);
                    float xHeight = ctx.getXHeight(font);
                    absVal = relVal * xHeight;
                }

                break;
            case CSSPrimitiveValue.CSS_PERCENTAGE:
                // percentage depends on the property this value belongs to
                if (cssName == CSSName.VERTICAL_ALIGN) {
                    relVal = style.getParent().getLineHeight(ctx);
                } else if (cssName == CSSName.FONT_SIZE) {
                    // same as with EM
                    FontSpecification parentFont = style.getParent().getFont(ctx);
                    baseValue = ctx.getFontSize2D(parentFont);
                } else if (cssName == CSSName.LINE_HEIGHT) {
                    FontSpecification font = style.getFont(ctx);
                    baseValue = ctx.getFontSize2D(font);
                }
                absVal = (relVal / 100F) * baseValue;

                break;
            default:
                // nothing to do, we only convert those listed above
                XRLog.cascade(Level.SEVERE,
                        "Asked to convert " + cssName + " from relative to absolute, " +
                        " don't recognize the datatype " +
                        "'" + ValueConstants.stringForSACPrimitiveType(primitiveType) + "' "
                        + primitiveType + "(" + stringValue + ")");
        }
        //assert (new Float(absVal).intValue() >= 0);

        if (cssName == CSSName.FONT_SIZE) {
            XRLog.cascade(Level.FINEST, cssName + ", relative= " +
                    relVal + " (" + stringValue + "), absolute= "
                    + absVal);
        } else {
            XRLog.cascade(Level.FINEST, cssName + ", relative= " +
                    relVal + " (" + stringValue + "), absolute= "
                    + absVal + " using base=" + baseValue);
        }

        // round down. (CHECK: Why? Is this always appropriate? - tobe)
        double d = Math.floor((double) absVal);
        absVal = new Float(d).floatValue();
        return absVal;
    }


    //TODO: this stuff is a bit of a mess
    private static float getFontSizeForXHeight(CalculatedStyle style, CssContext ctx, float xHeight) {
        FontSpecification f = new FontSpecification();

        //can't set size now
        // ASK: why don't we just use the style's getFont()? we're already caching these values
        // e.g.
        // return ctx.getFontSizeForXHeight(style.getParent().getFont(ctx), style.getFont(ctx), xHeight);
        //ANSWER: when the font is already cached, this method never gets called
        f.fontWeight = style.getIdent(CSSName.FONT_WEIGHT);
        f.families = style.asStringArray(CSSName.FONT_FAMILY);

        f.fontStyle = style.getIdent(CSSName.FONT_STYLE);
        f.variant = style.getIdent(CSSName.FONT_VARIANT);

        return ctx.getFontSizeForXHeight(style.getParent().getFont(ctx), f, xHeight);
    }
}

/*
 * $Id$
 */