package com.forehapp.store.general.utils;

public class SlugUtils {

    private SlugUtils() {}

    public static String slugify(String input) {
        if (input == null) return "";
        return input.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s-]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
