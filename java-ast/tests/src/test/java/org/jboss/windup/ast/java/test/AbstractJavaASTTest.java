package org.jboss.windup.ast.java.test;

import java.util.HashSet;
import java.util.Set;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public abstract class AbstractJavaASTTest
{
    private Set<String> libraryPaths = new HashSet<>();
    private Set<String> sourcePaths;

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.ast:windup-java-ast"),
                @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addPackage(AbstractJavaASTTest.class.getPackage())
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.ast:windup-java-ast"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Before
    public void before()
    {
        sourcePaths = new HashSet<>();
        sourcePaths.add("src/test/resources");
    }

    Set<String> getLibraryPaths()
    {
        return libraryPaths;
    }

    Set<String> getSourcePaths()
    {
        return sourcePaths;
    }

}
