package org.xhtmlrenderer.chrome;

import org.jspecify.annotations.Nullable;

/**
 * Minimal JSON helpers — enough to read a handful of fields from DevTools
 * Protocol messages without pulling in a JSON library. Not a general parser.
 */
final class Json {
    private Json() {
    }

    @Nullable
    static Long extractLong(String json, String field) {
        int start = findFieldValueStart(json, field);
        if (start < 0) return null;
        int end = start;
        if (end < json.length() && json.charAt(end) == '-') end++;
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        if (end == start) return null;
        try {
            return Long.parseLong(json.substring(start, end));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nullable
    static String extractStringField(String json, String field) {
        int start = findFieldValueStart(json, field);
        if (start < 0 || start >= json.length() || json.charAt(start) != '"') return null;
        StringBuilder out = new StringBuilder();
        int i = start + 1;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '\\') {
                if (i + 1 >= json.length()) return null;
                char next = json.charAt(i + 1);
                switch (next) {
                    case '"': out.append('"'); break;
                    case '\\': out.append('\\'); break;
                    case '/': out.append('/'); break;
                    case 'n': out.append('\n'); break;
                    case 't': out.append('\t'); break;
                    case 'r': out.append('\r'); break;
                    case 'b': out.append('\b'); break;
                    case 'f': out.append('\f'); break;
                    default: out.append(next); break;
                }
                i += 2;
            } else if (c == '"') {
                return out.toString();
            } else {
                out.append(c);
                i++;
            }
        }
        return null;
    }

    @Nullable
    static String extractObjectField(String json, String field) {
        int start = findFieldValueStart(json, field);
        if (start < 0 || start >= json.length() || json.charAt(start) != '{') return null;
        int depth = 0;
        int end = start;
        boolean inString = false;
        boolean escape = false;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (inString) {
                if (escape) escape = false;
                else if (c == '\\') escape = true;
                else if (c == '"') inString = false;
            } else {
                if (c == '"') inString = true;
                else if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) return json.substring(start, end + 1);
                }
            }
            end++;
        }
        return null;
    }

    static String escape(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\t': sb.append("\\t"); break;
                case '\r': sb.append("\\r"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        return sb.toString();
    }

    /** Index in {@code json} after {@code "<field>":} (whitespace tolerant), or -1. */
    private static int findFieldValueStart(String json, String field) {
        String key = "\"" + field + "\"";
        int i = 0;
        while ((i = json.indexOf(key, i)) >= 0) {
            int j = i + key.length();
            while (j < json.length() && Character.isWhitespace(json.charAt(j))) j++;
            if (j < json.length() && json.charAt(j) == ':') {
                j++;
                while (j < json.length() && Character.isWhitespace(json.charAt(j))) j++;
                return j;
            }
            i++;
        }
        return -1;
    }
}
