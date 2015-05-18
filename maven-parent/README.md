maven-parent: Generally usefull maven parent artifact
=====================================================

Part of [ozymandias](https://github.com/sviperll/ozymandias).

Usage
-----

Set `maven-parent` as parent project and activate required profiles with `activateparentprofiles` property:

```xml
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
