maven-profiledep-extension
==========================

Part of [ozymandias](https://github.com/sviperll/ozymandias).

Tool to modularize maven build configuration by splitting it into
a set of small inter-dependent profiles.

Modularization is achieved with two features provided by this extension:

 * support for dependencies between different profiles in maven build.
 * a way to activate profiles provided by parent pom from current pom.

This extension can be observed with maven-help-plugin.

You can see active profiles like this:

````
$ mvn help:active-profiles
````

Let's pretend that this will produce following output:

````
The following profiles are active:

 - java6 (source: groupId:artifactId:version)
````

These are default profiles configured with `activeByDefault` tag.
Now when you specify profiles on command line, default profiles are not activated,
but replaced with specified profiles. Here we should expect two profiles to be
active.

````
$ mvn '-Pjava7,bootclasspath' help:active-profiles
````

Instead 3 profiles are activated when this extension is enabled:

````
The following profiles are active:

 - java7 (source: groupId:artifactId:version)
 - java7-bootclasspath (source: groupId:artifactId:version)
 - fork-javac (source: groupId:artifactId:version)
````

Activation is based on dependency information. To specify that one profile
depends on another you put special `profiledep` property into profile
definition. `profiledep` is a list of comma separated profile ids.
You can prepend exclamation mark (`!`) to profile id in `profiledep` list to
specify _conflicts-relationship_. When conflicting profiles are activated
error is raised by this extension.

Note that there is no `bootclasspath` profile in the above list.
`bootclasspath` is a _virtual_ profile. A profile can provide any number
of virtual profiles by specifying `profileprovide` property in profile
definition. If more than one profile provides the same virtual profile
only one is actually activated.

Here is an example of profile definitions:

````xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <!-- ... -->
    <build>
        <!-- ... -->
    </build>
    <profiles>
        <profile>
            <id>java6-bootclasspath</id>
            <properties>
                <profileprovide>bootclasspath</profileprovide>
                <profiledep>java6,fork-javac</profiledep>
            </properties>
            <build>
                <!-- ... -->
            </build>
        </profile>
        <profile>
            <id>java7-bootclasspath</id>
            <properties>
                <profileprovide>bootclasspath</profileprovide>
                <profiledep>java7,fork-javac</profiledep>
            </properties>
            <build>
                <!-- ... -->
            </build>
        </profile>

        <profile>
            <id>java8-bootclasspath</id>
            <properties>
                <profileprovide>bootclasspath</profileprovide>
                <profiledep>java8,fork-javac</profiledep>
            </properties>
            <build>
                <!-- ... -->
            </build>
        </profile>

        <profile>
            <id>java6</id>
            <properties>
                <profileprovide>java-version</profileprovide>
            </properties>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <build>
                <!-- ... -->
            </build>
        </profile>
        <profile>
            <id>java7</id>
            <properties>
                <profileprovide>java-version</profileprovide>
            </properties>
            <build>
                <!-- ... -->
            </build>
        </profile>
        <profile>
            <id>java8</id>
            <properties>
                <profileprovide>java-version</profileprovide>
            </properties>
            <build>
                <!-- ... -->
            </build>
        </profile>

        <profile>
            <id>fork-javac</id>
            <build>
                <!-- ... -->
            </build>
        </profile>

    </profiles>
</project>
````

Profile dependencies work across single pom.xml file. You can't depend
on profile from parent pom.

Configuring profiles from pom.xml
---------------------------------

You can declare a set of interdependent profiles in your parent pom
and activate them in inherited pom. Special `activateparentprofiles` property is
used for it.

````xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.github.sviperll</groupId>
        <artifactId>maven-parent</artifactId>
        <version>0.6</version>
    </parent>
    <groupId>group</groupId>
    <artifactId>myartifact</artifactId>
    <version>version</version>
    <!-- ... -->
    <properties>
        <activateparentprofiles>java6,nexus-deploy</activateparentprofiles>
        <!-- ... -->
    </properties>
    <!-- ... -->
</project>
````

`java6` and `nexus-deploy` are two profiles defined in
`maven-parent` pom.
With such declaration given profiles are always activated when building
`myartifact`.
Other profiles from `maven-parent` can be activated as
required by profile dependencies.

`activateparentprofiles` affects only profiles from parent pom
and parent of parent pom etc, but never affects current pom.

`activateparentprofiles` can contain negative directives, like this:

````xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.github.sviperll</groupId>
        <artifactId>maven-parent</artifactId>
        <version>0.6</version>
    </parent>
    <groupId>group</groupId>
    <artifactId>myartifact</artifactId>
    <version>version</version>
    <!-- ... -->
    <properties>
        <activateparentprofiles>java6,!mustache</activateparentprofiles>
        <!-- ... -->
    </properties>
    <!-- ... -->
</project>
````

In this example you can activate `mustache` profile from `myartifact` pom, like
this:

````
$ mvn -P mustache verify
````

This command will activate `mustache` profile from `myartifact` pom, but
never `mustache` profile from `myparent` pom.

This can be used to override profile from parent pom, for example.

Active by default profiles
--------------------------

maven-profileddep-extension always try to activate all profiles
marked as activeByDefault. Only those profiles that do not conflict with
explicitly activated profiles are activated.

Installation
------------

### Maven 3.3.1 ###

Maven 3.3.1 allows core-dependencies to be specified in `extensions.xml` file.
See [this blog post](http://takari.io/2015/03/19/core-extensions.html).

Easiest way is to use
[coreext-maven-plugin](https://github.com/sviperll/ozymandias/tree/master/coreext-maven-plugin),
like this:

````xml
            <plugin>
                <groupId>com.github.sviperll</groupId>
                <artifactId>coreext-maven-plugin</artifactId>
                <version>0.6</version>
                <configuration>
                    <extensions combine.children="append">
                        <!-- ... -->
                        <extension>
                            <groupId>com.github.sviperll</groupId>
                            <artifactId>maven-profiledep-extension</artifactId>
                            <version>${project.version}</version>
                        </extension>
                        <!-- ... -->
                    </extensions>
                </configuration>
                <executions>
                    <execution>
                        <id>check-coreext</id>
                        <goals>
                            <goal>check</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
````

coreext-plugin will modify `.mvn/extensions.xml` as required.

If you wish you can manually write `.mvn/extensions.xml` file like this:

````xml
<?xml version="1.0" encoding="UTF-8"?>
<extensions>
  <extension>
    <groupId>com.github.sviperll</groupId>
    <artifactId>maven-profiledep-extension</artifactId>
    <version>0.6</version>
  </extension>
</extensions>
````

Version 0.6 of maven-profiledep-extension is available from maven central.
No additional configuration is required.

### Older maven versions ###

Older maven versions require `maven-profiledep-extension.jar` file to be placed
in `$MAVEN_HOME/lib/ext` directory.
