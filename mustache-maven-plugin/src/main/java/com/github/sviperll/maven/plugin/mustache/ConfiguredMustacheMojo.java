package com.github.sviperll.maven.plugin.mustache;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.apache.maven.project.MavenProject;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

abstract class ConfiguredMustacheMojo
    extends MustacheMojo
{

    @Parameter
    List<Context> contexts;

    /**
     * Encoding to read and write files.
     */
    @Parameter(property = "encoding", defaultValue = "UTF-8")
    String encoding;

    /**
    * The current Maven project or the super pom.
    */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;
}
