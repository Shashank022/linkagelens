package io.github.shashank022.linkagelens.util;

public final class Json {
    private Json() {}
    public static String q(String value) {
        if (value == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> { if (c < 32) sb.append(String.format("\\u%04x", (int)c)); else sb.append(c); }
            }
        }
        return sb.append('"').toString();
    }
}
