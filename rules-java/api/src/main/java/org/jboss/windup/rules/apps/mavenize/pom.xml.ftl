<#-- This file should be kept aligned with JBoss EAP Quickstarts. -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

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
    <description>${pom.description}</description>

    <properties>
        <!-- maven-compiler-plugin -->
        <maven.compiler.target>1.8</maven.compiler.target>
        <maven.compiler.source>1.8</maven.compiler.source>
    </properties>

    <#if pom.packaging = "pom">
    <modules>
        <#list pom.submodules as module>
        <module>${module.path}</module>
        </#list>
    </modules>
    </#if>

    <#if pom.bom>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>${pom.bom.groupId}</groupId>
                <artifactId>${pom.bom.artifactId}</artifactId>
                <version>${pom.bom.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    </#if>

    <#if pom.root>
    <!-- Activate JBoss Product Maven repository.

        NOTE: Configuring the Maven repository in the pom.xml file is not a recommended procedure
        and is only done here to make it easier.
        See the section entitled 'Use the Maven Repository' in the Development Guide for Red Hat JBoss EAP:
        https://access.redhat.com/documentation/en/jboss-enterprise-application-platform/
    -->
    <repositories>
        <repository>
            <id>jboss-developer-staging-repository</id>
            <url>http://jboss-developer.github.io/temp-maven-repo/</url>
        </repository>
        <repository>
            <id>jboss-enterprise-maven-repository</id>
            <url>https://maven.repository.redhat.com/ga/</url>
        </repository>
    </repositories>

    <pluginRepositories>
        <pluginRepository>
            <id>jboss-developer-staging-repository</id>
            <url>http://jboss-developer.github.io/temp-maven-repo/</url>
        </pluginRepository>
        <pluginRepository>
            <id>jboss-enterprise-maven-repository</id>
            <url>https://maven.repository.redhat.com/ga/</url>
        </pluginRepository>
    </pluginRepositories>
    </#if>

</project>
