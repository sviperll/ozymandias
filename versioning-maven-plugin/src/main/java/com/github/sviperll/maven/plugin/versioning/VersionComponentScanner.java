/*
 * Copyright 2015 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.maven.plugin.versioning;

import java.util.Arrays;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
class VersionComponentScanner {
    private static final char[] numberChars = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    static VersionComponentScanner createInstance(VersionSchema schema, String string) {
        VersionComponentInstance finalComponentInstance = schema.finalVersionComponentInstance("");
        return new VersionComponentScanner(schema, string.toCharArray(), 0, finalComponentInstance);
    }
    private final VersionSchema schema;
    private final char[] chars;
    private int position;
    private final VersionComponentInstance finalComponentInstance;

    VersionComponentScanner(VersionSchema schema, char[] chars, int position, VersionComponentInstance finalComponentInstance) {
        this.schema = schema;
        this.chars = chars;
        this.position = position;
        this.finalComponentInstance = finalComponentInstance;
    }

    VersionComponentInstance getNextComponentInstance() {
        if (position == chars.length)
            return finalComponentInstance;
        else {
            String separator = "";
            if (chars[position] == '-' || chars[position] == '.') {
                separator = String.valueOf(chars[position]);
                position++;
            }
            if (position == chars.length || chars[position] == '-' || chars[position] == '.')
                return new VersionComponentInstance(separator, finalComponentInstance.component());
            else {
                if (isNumberChar(chars[position])) {
                    int i = 0;
                    int n = 0;
                    int[] v = new int[10];
                    while (position < chars.length && isNumberChar(chars[position])) {
                        while (position < chars.length && isNumberChar(chars[position])) {
                            n = n * 10 + numberCharIndex(chars[position]);
                            position++;
                        }
                        if (i >= v.length)
                            v = Arrays.copyOf(v, i + i / 2);
                        v[i] = n;
                        i++;
                        n = 0;
                        if (position < chars.length - 1 && chars[position] == '.' && isNumberChar(chars[position+1]))
                            position++;
                    }
                    return schema.numbersComponentInstance(separator, Arrays.copyOf(v, i));
                } else {
                    StringBuilder builder = new StringBuilder();
                    while (position < chars.length && chars[position] != '-' && chars[position] != '.' && !isNumberChar(chars[position])) {
                        builder.append(chars[position]);
                        position++;
                    }
                    return schema.suffixComponentInstance(separator, builder.toString());
                }
            }
        }
    }

    private boolean isNumberChar(char c) {
        return numberCharIndex(c) >= 0;
    }

    private int numberCharIndex(char c) {
        for (int i = 0; i < numberChars.length; i++) {
            if (c == numberChars[i])
                return i;
        }
        return -1;
    }

    boolean hasMoreComponents() {
        return position < chars.length;
    }
}
