package tech.tresearchgroup.palila.controller;

public class StringController {
    public static String toCamelCase(String value) {
        String withoutUnderscores = value.replaceAll("_", " ");
        String[] words = withoutUnderscores.split("[\\W_]+");
        StringBuilder builder = new StringBuilder();
        for (String s : words) {
            String word = s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
            builder.append(word).append(" ");
        }
        return builder.toString();
    }
}
