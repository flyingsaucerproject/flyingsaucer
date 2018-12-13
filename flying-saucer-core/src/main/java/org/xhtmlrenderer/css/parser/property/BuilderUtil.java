package org.xhtmlrenderer.css.parser.property;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.parser.CSSParseException;
import org.xhtmlrenderer.css.parser.FSFunction;

/**
 * Static utility functions to check types, etc for builders to use.
 */
public class BuilderUtil {
	private BuilderUtil() {
	}


	public static boolean isLength(CSSPrimitiveValue value) {
		int unit = value.getPrimitiveType();
		return unit == CSSPrimitiveValue.CSS_EMS
				|| unit == CSSPrimitiveValue.CSS_EXS
				|| unit == CSSPrimitiveValue.CSS_PX
				|| unit == CSSPrimitiveValue.CSS_IN
				|| unit == CSSPrimitiveValue.CSS_CM
				|| unit == CSSPrimitiveValue.CSS_MM
				|| unit == CSSPrimitiveValue.CSS_PT
				|| unit == CSSPrimitiveValue.CSS_PC
				|| (unit == CSSPrimitiveValue.CSS_NUMBER && value
						.getFloatValue(CSSPrimitiveValue.CSS_IN) == 0.0f);
	}

    public static void checkFunctionsAllowed(final FSFunction func, String... allowed)
    {
        for (String allow : allowed)
        {
            if (allow.equals(func.getName()))
                return;
        }

        throw new CSSParseException(String.format("Function ({0}) not supported here", func.getName()), -1);

    }

}
