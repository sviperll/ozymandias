/*
 * Copyright 2015 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.maven.plugin.versioning;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@SuppressWarnings("serial")
public class UndefinedStability extends Exception {
    UndefinedStability(String message) {
        super(message);
    }
}
