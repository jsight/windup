<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <properties>
    </properties>

    <parent>
        <groupId>${pom.parent.groupId}</groupId>
        <artifactId>${pom.parent.artifactId}</artifactId>
        <version>${pom.parent.version}</version>
    </parent>

    <groupId>${pom.groupId}</groupId>
    <artifactId>${pom.artifactId}</artifactId>
    <version>${pom.version}</version>
    <packaging>${pom.packaging}</packaging>

    <name>${pom.name}</name>

    <dependencyManagement>
        <dependencies>

        <!-- JBoss distributes a complete set of Java EE APIs including a Bill
            of Materials (BOM). A BOM specifies the versions of a "stack" (or a collection)
            of artifacts. We use this here so that we always get the correct versions
            of artifacts. Here we use the jboss-eap-javaee7 stack (you can
            read this as the JBoss stack of the Java EE APIs and related components.  -->
        <dependency>
            <groupId>org.jboss.bom</groupId>
            <artifactId>jboss-eap-javaee7</artifactId>
            <version>7.0.0-build-12</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>

        <#list pom.dependencies as dep>
            <!-- ${dep.description!} -->
            <dependency>
                <groupId>${dep.groupId}</groupId>
                <artifactId>${dep.artifactId}</artifactId>
                <version>${dep.version}</version>
                <type>${dep.packaging}</type>
                <scope>import</scope>
            </dependency>

        </#list>
        </dependencies>
    </dependencyManagement>
</project>
