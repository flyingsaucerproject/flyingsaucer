package org.xhtmlrenderer.css.style.derived;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.style.DerivedValue;

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
    
    public StringValue(CSSName name, PropertyValue value) {
        super(name, value.getPrimitiveType(), value.getCssText(), value.getStringValue());
        if (value.getStringArrayValue() != null) {
            _stringAsArray = value.getStringArrayValue();   
        }
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

    public String toString() {
        return getStringValue();
    }
}
