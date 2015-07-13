mustache-maven-plugin: Maven plugin to render mustache templates at build-time
==============================================================================

Part of [ozymandias](https://github.com/sviperll/ozymandias).

Renders mustache templates during maven build with data from json- or .properties-files.

Goals
-----

 * *mustache:render* render configured templates in provided context

Context file formats
--------------------

 * *json* JSON format is processed like original mustache specification demands it

 * *properties* Properties files are converted to object hierarchies. For example following property file

   ```
   project.version.stable=0.23
   project.version.unstable=0.24-rc1
   project.description=Hello World
   ```

   is processed precisely like following JSON-file

   ```
   {"project": {"version": {"stable": "0.23", "unstable": "0.24-rc1"}}, "description": "Hello World"}
   ```

Executions example
------------------

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
                <artifactId>mustache-maven-plugin</artifactId>
                <version>0.9</version>
                <inherited>false</inherited>
                <configuration>
                    <!-- encoding for reading and writing files -->
                    <encoding>UTF-8</encoding>

                    <contexts>
                        <context>
                            <!-- file with field values to put into templates -->
                            <file>project.properties</file>

                            <!-- Type of context file. Supported values: json, properties -->
                            <type>properties</type>

                            <templates>
                                <template>
                                    <inputFile>README.md.mustache</inputFile>
                                    <outputFile>README.md</outputFile>
                                </template>
                                <template>
                                    <inputFile>my-module1/README.md.mustache</inputFile>
                                    <outputFile>my-module1/README.md</outputFile>
                                </template>
                            </templates>
                        </context>
                    </contexts>
                </configuration>
                <executions>
                    <execution>
                        <id>render-readmes</id>
                        <goals>
                            <goal>render</goal>
                        </goals>
                        <!-- binded with generate-resources phase by default -->
                        <!--
                            <phase>generate-resources</phase>
                        -->
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
