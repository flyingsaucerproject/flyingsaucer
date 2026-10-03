package org.xhtmlrenderer.util;

import org.junit.jupiter.api.Test;

import java.awt.RenderingHints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ConfigurationTest {
    @Test
    public void stringValue() {
        assertThat(Configuration.valueFor("xr.css.user-agent-default-css", "the-default")).isEqualTo("/resources/css/");
        assertThat(Configuration.valueFor("xr.css.user-agent-default-CSS", "the-default")).isEqualTo("the-default");
    }

    @Test
    public void valueOnlyDefinedInSystemProperties() {
        assertThat(Configuration.valueFor("xr.test-config-from-system-property", "the-default")).isEqualTo("from-system-property");
    }

    @Test
    public void byteValue() {
        assertThat(Configuration.valueAsByte("xr.test-config-byte", (byte) 15)).isEqualTo(8);
        assertThat(Configuration.valueAsByte("xr.test-config-BYTE", (byte) 15)).isEqualTo(15);
    }

    @Test
    public void shortValue() {
        assertThat(Configuration.valueAsShort("xr.test-config-short", (short) 20)).isEqualTo(16);
        assertThat(Configuration.valueAsShort("xr.test-config-SHORT", (short) 20)).isEqualTo(20);
    }

    @Test
    public void intValue() {
        assertThat(Configuration.valueAsInt("xr.test-config-int", 25)).isEqualTo(100);
        assertThat(Configuration.valueAsInt("xr.test-config-INT", 25)).isEqualTo(25);
    }

    @Test
    public void longValue() {
        assertThat(Configuration.valueAsLong("xr.test-config-long", 30L)).isEqualTo(2000);
        assertThat(Configuration.valueAsLong("xr.test-config-LONG", 30L)).isEqualTo(30L);
    }

    @Test
    public void floatValue() {
        assertThat(Configuration.valueAsFloat("xr.test-config-float", 45.5F)).isEqualTo(3000.25F);
        assertThat(Configuration.valueAsFloat("xr.test-config-FLOAT", 45.5F)).isEqualTo(45.5F);
    }

    @Test
    public void doubleValue() {
        assertThat(Configuration.valueAsDouble("xr.test-config-double", 50.75D)).isEqualTo(4000.50D);
        assertThat(Configuration.valueAsDouble("xr.test-config-DOUBLE", 50.75D)).isEqualTo(50.75D);
    }

    @Test
    public void types() {
        assertThat(Configuration.isTrue("xr.test-config-boolean", false)).isTrue();
        assertThat(Configuration.isTrue("xr.test-config-BOOLEAN", false)).isFalse();
    }

    @Test
    public void classConstantValue() {
        assertThat(Configuration.valueFromClassConstant("xr.text.aa-rendering-hint", "the-default"))
                .isSameAs(RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        assertThat(Configuration.valueFromClassConstant("xr.test-config-UNDEFINED", "the-default"))
                .isEqualTo("the-default");
    }

    @Test
    public void classConstantWithoutPackage() {
        assertThatThrownBy(() -> Configuration.valueFromClassConstant("xr.test-config-constant-without-package", "the-default"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid value of configuration property xr.test-config-constant-without-package: " +
                        "\"RenderingHints.VALUE_TEXT_ANTIALIAS_ON\", expected <fully qualified class name>.<public static constant name>")
                .hasCauseInstanceOf(ClassNotFoundException.class);
    }

    @Test
    public void missingClassConstant() {
        assertThatThrownBy(() -> Configuration.valueFromClassConstant("xr.test-config-missing-constant", "the-default"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid value of configuration property xr.test-config-missing-constant: " +
                        "\"java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_HGRB\", expected <fully qualified class name>.<public static constant name>")
                .hasCauseInstanceOf(NoSuchFieldException.class);
    }

    @Test
    public void fractionalFontMetricsAreEnabledByDefault() {
        assertThat(Configuration.isTrue("xr.text.fractional-font-metrics", false)).isTrue();
    }
}
