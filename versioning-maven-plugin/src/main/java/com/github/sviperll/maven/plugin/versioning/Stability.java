/*
 * Copyright 2015 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.maven.plugin.versioning;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class Stability {
    String defaultStability = "unstable";
    List<String> stableKinds = Collections.emptyList();
    List<String> unstableKinds = Collections.emptyList();
}
