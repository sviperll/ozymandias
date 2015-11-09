maven-profiledep-extension
==========================

Part of [ozymandias](https://github.com/sviperll/ozymandias).

Tool to modularize maven build configuration by splitting it into
a set of small inter-dependent profiles.

Modularization is achieved with several features provided by this extension:

 * support for dependencies between different profiles in maven build.
 * a way to activate profiles provided by parent pom from current pom.
 * a way to make profile always activate, except when explicitly deactivated

See [universal-maven-parent](https://github.com/sviperll/universal-maven-parent) project for example of `maven-profiledep-extension` usage.

Active by default profiles
--------------------------

Maven provides `activeByDefault` activation tag for profiles.
The way `activeByDefault` works is confusing and it's a best practice to avoid `activeByDefault`.

Profiledep instead provides `profile.active' property to make profile always active.

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

`Java6` profile is active because it is marked with `profile.active` property.
Like this

````xml
    <profile>
        <id>java6</id>
        <properties>
            <profile.active>true</profile.active>
        </properties>
    </profile>
````

Now let's try to specify some other profile on command line.

````
$ mvn -P nexus-deploy help:active-profiles
````

The following output is produced. 

````
The following profiles are active:

 - java6 (source: groupId:artifactId:version)
 - nexus-deploy (source: groupId:artifactId:version)
````

Note that `java6` profile is activated even so some
other profile is activated. This behavior differs from
maven's `activeByDefault` handling.

You should probably always use `profile.active` property
instead of `activeByDefault` since it's behavior is less confusing.

With maven-profiledep-extension profiles can have dependencies and
can conflict with each other.

Suppose that `profile.activate.default` is enabled
and you try to run

````
$ mvn -P java7 help:active-profiles
````

You will get the following error:

````
[ERROR]   The project groupId:artifactId:version (/home/user/code/project) has 1 error
[ERROR]     > ---.
[ERROR]     >  `----Can't provide java-version
[ERROR]     >     `----more than one profile provides it
[ERROR]     >        |----java7
[ERROR]     >        `----java6
[ERROR]     @ groupId:artifactId:version
[ERROR] 
````

The problem is that only one `java-version` can exist.
To solve this problem you must explicitly deactivate some profile:

````
$ mvn -P '!java6,java7' help:active-profiles
````

````
The following profiles are active:

 - java7 (source: groupId:artifactId:version)
````

Profile dependencies
--------------------

Dependency information can cause additional profile activation.

````
$ mvn '-P!java6,java7,bootclasspath' help:active-profiles
````

3 profiles are activated:

````
The following profiles are active:

 - java7 (source: groupId:artifactId:version)
 - java7-bootclasspath (source: groupId:artifactId:version)
 - fork-javac (source: groupId:artifactId:version)
````

Activation is based on dependency information. To specify that one profile
depends on another you put special `profile.depends` property into profile
definition. `profile.depends` is a list of comma separated profile ids,
like the one you specify in command line.
You can prepend exclamation mark (`!`) to profile id in `profile.depends` list to
specify _conflicts-relationship_. When conflicting profiles are activated
error is raised by this extension.

Note that there is no `bootclasspath` profile in the above list.
`bootclasspath` is a _virtual_ profile. A profile can provide any number
of virtual profiles by specifying `profile.provides` property in profile
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
                <profile.provides>bootclasspath</profile.provides>
                <profile.depends>java6,fork-javac</profile.depends>
            </properties>
            <build>
                <!-- ... -->
            </build>
        </profile>
        <profile>
            <id>java7-bootclasspath</id>
            <properties>
                <profile.provides>bootclasspath</profile.provides>
                <profile.depends>java7,fork-javac</profile.depends>
            </properties>
            <build>
                <!-- ... -->
            </build>
        </profile>

        <profile>
            <id>java8-bootclasspath</id>
            <properties>
                <profile.provides>bootclasspath</profile.provides>
                <profile.depends>java8,fork-javac</profile.depends>
            </properties>
            <build>
                <!-- ... -->
            </build>
        </profile>

        <profile>
            <id>java6</id>
            <properties>
                <profile.provides>java-version</profile.provides>
                <profile.active>true</profile.active>
            </properties>
            <build>
                <!-- ... -->
            </build>
        </profile>
        <profile>
            <id>java7</id>
            <properties>
                <profile.provides>java-version</profile.provides>
            </properties>
            <build>
                <!-- ... -->
            </build>
        </profile>
        <profile>
            <id>java8</id>
            <properties>
                <profile.provides>java-version</profile.provides>
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

Activating profiles from pom.xml
---------------------------------

You can declare a set of interdependent profiles in your parent pom
and activate them in inherited pom. Special `parent.profile.activate` property is
used for it. This property is a list of profile IDs. It works just the same
way you specify profiles in command line.

````xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.github.sviperll</groupId>
        <artifactId>maven-parent</artifactId>
        <version>0.10</version>
    </parent>
    <groupId>group</groupId>
    <artifactId>myartifact</artifactId>
    <version>version</version>
    <!-- ... -->
    <properties>
        <parent.profile.activate>java6,nexus-deploy</parent.profile.activate>
        <!-- ... -->
    </properties>
    <!-- ... -->
</project>
````

`java6` and `nexus-deploy` are two profiles defined in
`maven-parent` pom.
With such declaration given profiles are activated when building
`myartifact`.
Other profiles from `maven-parent` can be activated as
required by profile dependencies.

`parent.profile.activate` affects only profiles from parent pom
and parent of parent pom etc, but never affects current pom.

`parent.profile.activate` can contain negative directives, like this:

````xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.github.sviperll</groupId>
        <artifactId>maven-parent</artifactId>
        <version>0.10</version>
    </parent>
    <groupId>group</groupId>
    <artifactId>myartifact</artifactId>
    <version>version</version>
    <!-- ... -->
    <properties>
        <parent.profile.activate>java6,!mustache</parent.profile.activate>
        <!-- ... -->
    </properties>
    <!-- ... -->
</project>
````

This means that `mustache` profile will not be activated in parent pom
even if it is active by default or activated by some maven activation
mechanism.

You can still activate this profile if you list it in command line

````
$ mvn -P mustache verify
````

Command line profile list has greater priority than `parent.profile.activate` list.
In the same way `parent.profile.activate` list from inherited pom has
greater priority then `parent.profile.activate` list from it's parent pom.

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
                <version>0.10</version>
                <configuration>
                    <extensions combine.children="append">
                        <!-- ... -->
                        <extension>
                            <groupId>com.github.sviperll</groupId>
                            <artifactId>maven-profiledep-extension</artifactId>
                            <version>0.10</version>
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
    <version>0.10</version>
  </extension>
</extensions>
````

Version 0.10 of maven-profiledep-extension is available from maven central.
No additional configuration is required.

### Older maven versions ###

Older maven versions require `maven-profiledep-extension.jar` file to be placed
in `$MAVEN_HOME/lib/ext` directory.
