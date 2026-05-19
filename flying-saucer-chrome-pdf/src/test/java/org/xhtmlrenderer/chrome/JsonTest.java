package org.xhtmlrenderer.chrome;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.xhtmlrenderer.chrome.Json.escape;
import static org.xhtmlrenderer.chrome.Json.extractLong;
import static org.xhtmlrenderer.chrome.Json.extractObjectField;
import static org.xhtmlrenderer.chrome.Json.extractStringField;

class JsonTest {

    @Test
    void extractsLongFromMinifiedJson() {
        assertThat(extractLong("{\"id\":42,\"method\":\"Page.enable\"}", "id")).isEqualTo(42L);
    }

    @Test
    void extractsLongWithWhitespace() {
        assertThat(extractLong("{ \"id\" : 7 }", "id")).isEqualTo(7L);
    }

    @Test
    void extractsNegativeLong() {
        assertThat(extractLong("{\"n\":-12345}", "n")).isEqualTo(-12345L);
    }

    @Test
    void returnsNullForMissingField() {
        assertThat(extractLong("{\"other\":1}", "id")).isNull();
        assertThat(extractStringField("{\"other\":\"x\"}", "id")).isNull();
        assertThat(extractObjectField("{\"other\":{}}", "id")).isNull();
    }

    @Test
    void extractsStringField() {
        assertThat(extractStringField("{\"method\":\"Page.loadEventFired\"}", "method"))
                .isEqualTo("Page.loadEventFired");
    }

    @Test
    void extractsStringFieldWithWhitespace() {
        assertThat(extractStringField("{ \"method\" :  \"Page.enable\" }", "method"))
                .isEqualTo("Page.enable");
    }

    @Test
    void decodesEscapedCharsInString() {
        assertThat(extractStringField("{\"s\":\"a\\\"b\\\\c\\nd\"}", "s")).isEqualTo("a\"b\\c\nd");
        assertThat(extractStringField("{\"s\":\"\\t\\r\\b\\f\\/x\"}", "s")).isEqualTo("\t\r\b\f/x");
    }

    @Test
    void doesNotMatchFieldsThatAreSuffixes() {
        // "userAgentData" should not be returned when asking for "userAgent"
        String json = "{\"userAgentData\":\"X\",\"userAgent\":\"Y\"}";
        assertThat(extractStringField(json, "userAgent")).isEqualTo("Y");
    }

    @Test
    void extractsObjectField() {
        String json = "{\"result\":{\"data\":\"base64stuff\",\"n\":1},\"id\":1}";
        assertThat(extractObjectField(json, "result")).isEqualTo("{\"data\":\"base64stuff\",\"n\":1}");
    }

    @Test
    void extractsNestedObjectField() {
        String json = "{\"a\":{\"b\":{\"c\":1}}}";
        assertThat(extractObjectField(json, "a")).isEqualTo("{\"b\":{\"c\":1}}");
    }

    @Test
    void objectFieldRespectsBracesInsideStrings() {
        String json = "{\"err\":{\"message\":\"Bad }{ data\"}}";
        assertThat(extractObjectField(json, "err")).isEqualTo("{\"message\":\"Bad }{ data\"}");
    }

    @Test
    void escapesSpecialChars() {
        assertThat(escape("hello")).isEqualTo("hello");
        assertThat(escape("a\"b")).isEqualTo("a\\\"b");
        assertThat(escape("a\\b")).isEqualTo("a\\\\b");
        assertThat(escape("a\nb\tc\rd")).isEqualTo("a\\nb\\tc\\rd");
        assertThat(escape("\u0001")).isEqualTo("\\u0001");
    }
}
