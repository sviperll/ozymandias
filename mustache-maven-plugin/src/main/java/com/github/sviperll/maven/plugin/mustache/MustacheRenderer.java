/*
 * Copyright 2015 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.maven.plugin.mustache;

import com.github.mustachejava.Mustache;
import java.io.Writer;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class MustacheRenderer {
    private final Writer writer;
    MustacheRenderer(Writer writer) {
        this.writer = writer;
    }

    void render(Mustache mustache, Object context) {
        mustache.execute(writer, context);
    }

}
