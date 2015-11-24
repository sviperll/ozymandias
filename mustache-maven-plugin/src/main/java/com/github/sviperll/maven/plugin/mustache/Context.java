/*
 * Copyright 2015 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.maven.plugin.mustache;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class Context {
    /**
     * File used as a context for mustache templates.
     */
    File file;

    /**
     * Type of context file.
     *
     * <ul>
     *   <li>json
     *   <li>properties
     * </ul>
     */
    String type = "json";

    /**
     * File to read or write properties to.
     */
    List<Template> templates = Collections.emptyList();

}
