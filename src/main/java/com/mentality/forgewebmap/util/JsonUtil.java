package com.mentality.forgewebmap.util;

import java.util.List;
import java.util.Map;

/**
 * Minimal JSON serialization without external dependencies.
 * Only covers the subset needed for our API responses.
 */
public final class JsonUtil {

    private JsonUtil() {}

    public static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(escapeString(e.getKey())).append("\":");
            sb.append(valueToJson(e.getValue()));
        }
        sb.append('}');
        return sb.toString();
    }

    public static String toJson(List<?> list) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object item : list) {
            if (!first) sb.append(',');
            first = false;
            sb.append(valueToJson(item));
        }
        sb.append(']');
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static String valueToJson(Object value) {
        if (value == null) return "null";
        if (value instanceof Boolean || value instanceof Integer
                || value instanceof Long || value instanceof Double
                || value instanceof Float) {
            return value.toString();
        }
        if (value instanceof String s) {
            return '"' + escapeString(s) + '"';
        }
        if (value instanceof Map<?,?> m) {
            return toJson((Map<String, Object>) m);
        }
        if (value instanceof List<?> l) {
            return toJson(l);
        }
        // fallback: quoted toString
        return '"' + escapeString(value.toString()) + '"';
    }

    private static String escapeString(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
