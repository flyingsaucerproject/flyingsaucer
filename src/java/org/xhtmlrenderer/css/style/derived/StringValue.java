package org.xhtmlrenderer.css.style.derived;

import org.xhtmlrenderer.css.style.FSDerivedValue;
import org.xhtmlrenderer.css.style.DerivedValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.constants.CSSName;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Oct 17, 2005
 * Time: 2:10:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class StringValue extends DerivedValue {
    private String[] _stringAsArray;

    public StringValue (
            CSSName name,
            short cssSACUnitType,
            String cssText,
            String cssStringValue
    ) {
        super(name, cssSACUnitType, cssText, cssStringValue);
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

    public FSDerivedValue copyOf(CSSName cssName) {
        return new StringValue(cssName, getCssSacUnitType(), getStringValue(), getStringValue());
    }

    public String toString() {
        return getStringValue();
    }
}
