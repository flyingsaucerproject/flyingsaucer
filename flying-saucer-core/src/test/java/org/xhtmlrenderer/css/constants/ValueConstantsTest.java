package org.xhtmlrenderer.css.constants;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_EMS;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_MM;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_PERCENTAGE;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_PT;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_PX;

class ValueConstantsTest {
    @Test
    void stringForSACPrimitiveType() {
        assertThat(ValueConstants.stringForSACPrimitiveType(CSS_EMS)).isEqualTo("em");
        assertThat(ValueConstants.stringForSACPrimitiveType(CSS_PX)).isEqualTo("px");
        assertThat(ValueConstants.stringForSACPrimitiveType(CSS_PERCENTAGE)).isEqualTo("%");
        assertThat(ValueConstants.stringForSACPrimitiveType(CSS_PT)).isEqualTo("pt");
        assertThat(ValueConstants.stringForSACPrimitiveType(CSS_MM)).isEqualTo("mm");
    }
}