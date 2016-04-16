<?xml version="1.0" encoding="UTF-8"?>
<#--
pom: class Pom
-->
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <properties>
    </properties>

    <#if pom.parent??>
    <parent>
        <groupId>${pom.parent.coords.groupId}</groupId>
        <artifactId>${pom.parent.coords.artifactId}</artifactId>
        <version>${pom.parent.coords.version}</version>
    </parent>
    </#if>

    <groupId>${pom.coords.groupId}</groupId>
    <artifactId>${pom.coords.artifactId}</artifactId>
    <#if pom.coords.version??><version>${pom.coords.version}</version></#if><#-- Null if same as parent. -->
    <#if pom.coords.packaging??><packaging>${pom.coords.packaging}</packaging></#if>
    <#if pom.coords.classifier??><classifier>${pom.coords.classifier}</classifier></#if>

    <name>${pom.name!}</name>
    <description>${pom.description!}</description>

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

        <#list pom.dependencies as dep><#-- MavenCoords -->
            <dependency>
                <groupId>${dep.groupId}</groupId>
                <artifactId>${dep.artifactId}</artifactId>
                <version>${dep.version}</version>
                <#if dep.classifier??><classifier>${dep.classifier}</classifier></#if><#t>
                <#if (dep.packaging!"jar") != "jar"><type>${dep.packaging}</type></#if><#t>
            </dependency>

        </#list>
        </dependencies>
    </dependencyManagement>
</project>
