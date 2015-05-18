/*
 * Copyright 2015 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.maven.plugin.versioning;

import java.util.Arrays;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class VersionComponentScanner {
    private static final char[] numberChars = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.'};
    private static final VersionComponentInstance FINAL_INSTANCE = new VersionComponentInstance("", VersionComponent.finalVersion());
    static VersionComponentScanner createInstance(String string) {
        return new VersionComponentScanner(string.toCharArray(), 0);
    }
    private final char[] chars;
    private int position;

    VersionComponentScanner(char[] chars, int position) {
        this.chars = chars;
        this.position = position;
    }

    VersionComponentInstance getNextComponentInstance() {
        if (position == chars.length)
            return FINAL_INSTANCE;
        else {
            String separator = "";
            if (chars[position] == '-') {
                separator = "-";
                position++;
            }
            if (position == chars.length || chars[position] == '-')
                return new VersionComponentInstance(separator, VersionComponent.finalVersion());
            else {
                if (isNumberChar(chars[position])) {
                    int i = 0;
                    int n = 0;
                    int[] v = new int[10];
                    while (position < chars.length && isNumberChar(chars[position])) {
                        if (chars[position] == '.') {
                            while (i >= v.length)
                                v = Arrays.copyOf(v, v.length + v.length / 2);
                            v[i] = n;
                            i++;
                            n = 0;
                        } else {
                            n = n * 10 + numberCharIndex(chars[position]);
                        }
                        position++;
                    }
                    if (i >= v.length)
                        v = Arrays.copyOf(v, i + 1);
                    v[i] = n;
                    i++;
                    return new VersionComponentInstance(separator, VersionComponent.numbers(Arrays.copyOf(v, i)));
                } else {
                    StringBuilder builder = new StringBuilder();
                    while (position < chars.length && chars[position] != '-' && !isNumberChar(chars[position])) {
                        builder.append(chars[position]);
                        position++;
                    }
                    return new VersionComponentInstance(separator, VersionComponent.custom(builder.toString()));
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
