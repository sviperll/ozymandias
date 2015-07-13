coreext-maven-plugin: Plugin to install core maven extensions
=============================================================

Part of [ozymandias](https://github.com/sviperll/ozymandias).

Manages `.mvn/extensions.xml` file introduced in maven 3.3.1.
Checks if all required core maven extensions are installed and have required
version numbers.

Rewrites .mvn/extensions.xml if asked to.

Goals
-----

 * *coreext:check* checks if maven core extensions are install
   and have required version number.

 * *coreext:install* creates or overwrites `.mvn/extensions.xml` file
   by adding all required core extensions
   or correcting specified version numbers.

Example
-------

Error message will be raised if some core extensions are missing when
`coreext:check` is run.

For example:

````
[ERROR] Failed to execute goal com.github.sviperll:coreext-maven-plugin:0.10:check (default-cli) on project maven-parent:
[ERROR] Some required core extensions are not installed:
[ERROR] 
[ERROR] * com.github.sviperll:maven-profiledep-extension:0.10
[ERROR] 
[ERROR] Run
[ERROR] 
[ERROR] mvn com.github.sviperll:coreext-maven-plugin:install
[ERROR] 
[ERROR] to install missing core extensions
[ERROR] -> [Help 1]
````

You can bind `coreext:check` to some phase in you parent pom to make sure
that all your projects have required core extensions installed.

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    ...
    <build>
        ...
        <plugins>
            ...
            <plugin>
                <groupId>com.github.sviperll</groupId>
                <artifactId>coreext-maven-plugin</artifactId>
                <version>0.10</version>
                <configuration>
                    <!-- Required core extensions: -->
                    <extensions combine.children="append">
                        <extension>
                            <groupId>com.github.sviperll</groupId>
                            <artifactId>maven-profiledep-extension</artifactId>
                            <version>${project.version}</version>
                        </extension>
                        <extension>
                            <groupId>io.takari.maven</groupId>
                            <artifactId>takari-smart-builder</artifactId>
                            <version>0.4.0</version>
                        </extension>
                        <extension>
                            <groupId>io.takari.aether</groupId>
                            <artifactId>takari-concurrent-localrepo</artifactId>
                            <version>0.0.7</version>
                        </extension>
                    </extensions>
                </configuration>
                <executions>
                    <execution>
                        <id>check-coreext</id>
                        <goals>
                            <goal>check</goal>
                        </goals>
                        <!-- default phase is:  -->
                        <!--     <phase>validate</phase> -->
                    </execution>
                </executions>
            </plugin>
            ...
        </plugins>
        ...
    </build>
    ...
</project>

```

Running `coreext:install` will overwrite `.mvn/extensions.xml` file
to fix all found core extension problems.
