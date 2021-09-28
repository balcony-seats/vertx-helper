package com.github.rdjurin.vertx.application.support.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class StringHelper {

    private static final char ESCAPE = '\\';

    public static List<String> splitEnclosed(String str, char delimiter, String... enclosedBy) {

        Set<String> enclosedChars = Set.of(enclosedBy);
        if (str == null || "".equals(str)) { //if empty
            return List.of();
        }

        if (enclosedChars.stream().noneMatch(str::contains)) { //no enclosing
            return Arrays.stream(StringUtils.split(str, delimiter)).collect(Collectors.toList());
        }

        List<String> results = new ArrayList<>(10);

        boolean enclosed = false;
        boolean escaped = false;
        char currentEnclosed = 0;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (escaped) {
                escaped = false;
                builder.append(c);
                continue;
            }
            if (c == ESCAPE) {
                escaped = true;
                continue;
            }
            if (!enclosed) {
                if (enclosedChars.contains(String.valueOf(c))) {
                    enclosed = true;
                    currentEnclosed = c;
                    continue;
                }
                if (c == delimiter) {
                    results.add(builder.toString());
                    builder.setLength(0);
                    continue;
                }
            } else {
                if (c == currentEnclosed) {
                    enclosed = false;
                    currentEnclosed = 0;
                    continue;
                }
            }

            builder.append(c);
        }
        results.add(builder.toString());

        return results;
    }
}
