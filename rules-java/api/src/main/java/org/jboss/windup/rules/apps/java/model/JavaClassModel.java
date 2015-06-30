package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.annotations.gremlin.GremlinParam;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents a JavaClass, either from a .class file or a .java source file.
 * 
 */
@TypeValue(JavaClassModel.TYPE)
public interface JavaClassModel extends WindupVertexFrame
{
    String JAVA_METHOD = "javaMethod";
    String CLASS_FILE = "classFile";
    String ORIGINAL_SOURCE = "originalSource";
    String DECOMPILED_SOURCE = "decompiledSource";
    String IMPLEMENTS = "implements";
    String EXTENDS = "extends";
    String IMPORTS = "imports";
    String TYPE = "JavaClassResource";
    String QUALIFIED_NAME = "qualifiedName";
    String CLASS_NAME = "className";
    String PACKAGE_NAME = "packageName";
    String IS_PUBLIC = "isPublic";
    String MAJOR_VERSION = "majorVersion";
    String MINOR_VERSION = "minorVersion";

    /**
     * Indicates whether the class is declared "public".
     */
    @Property(IS_PUBLIC)
    Boolean isPublic();

    /**
     * Indicates whether the class is declared "public".
     */
    @Property(IS_PUBLIC)
    void setPublic(boolean isPublic);

    /**
     * Contains the simple name of the class (no package)
     */
    @Property(CLASS_NAME)
    void setSimpleName(String className);

    /**
     * Contains the simple name of the class (no package)
     */
    @Property(CLASS_NAME)
    String getClassName();

    /**
     * Contains the fully qualified name of the class
     */
    @Indexed
    @Property(QUALIFIED_NAME)
    public String getQualifiedName();

    /**
     * Contains the fully qualified name of the class
     */
    @Property(QUALIFIED_NAME)
    public void setQualifiedName(String qualifiedName);

    /**
     * Contains the class' package name
     */
    @Indexed
    @Property(PACKAGE_NAME)
    String getPackageName();

    /**
     * Contains the class' package name
     */
    @Property(PACKAGE_NAME)
    void setPackageName(String packageName);

    /**
     * Lists classes imported by this class
     */
    @Adjacency(label = IMPORTS, direction = Direction.OUT)
    void addImport(final JavaClassModel javaImport);

    /**
     * Lists classes imported by this class
     */
    @Adjacency(label = IMPORTS, direction = Direction.OUT)
    Iterable<JavaClassModel> getImports();

    /**
     * Lists classes extended by this class
     */
    @Adjacency(label = EXTENDS, direction = Direction.OUT)
    JavaClassModel getExtends();

    /**
     * Lists classes extended by this class
     */
    @Adjacency(label = EXTENDS, direction = Direction.OUT)
    void setExtends(final JavaClassModel javaFacet);

    /**
     * Lists classes implemented by this class
     */
    @Adjacency(label = IMPLEMENTS, direction = Direction.OUT)
    void addImplements(final JavaClassModel javaFacet);

    /**
     * Lists classes implemented by this class
     */
    @Adjacency(label = IMPLEMENTS, direction = Direction.OUT)
    Iterable<JavaClassModel> getImplements();

    /**
     * Contains the {@link JavaSourceFileModel} of the decompiled version of this file (assuming that it originally was decompiled from a .class file)
     */
    @Adjacency(label = DECOMPILED_SOURCE, direction = Direction.OUT)
    void setDecompiledSource(JavaSourceFileModel source);

    /**
     * Contains the {@link JavaSourceFileModel} of the decompiled version of this file (assuming that it originally was
     * decompiled from a .class file)
     */
    @Adjacency(label = DECOMPILED_SOURCE, direction = Direction.OUT)
    JavaSourceFileModel getDecompiledSource();

    /**
     * Contains the original source code of this file, assuming that it was originally provided in source form (and not
     * via a decompilation).
     */
    @Adjacency(label = ORIGINAL_SOURCE, direction = Direction.OUT)
    void setOriginalSource(JavaSourceFileModel source);

    /**
     * Contains the original source code of this file, assuming that it was originally provided in source form (and not
     * via a decompilation).
     */
    @Adjacency(label = ORIGINAL_SOURCE, direction = Direction.OUT)
    JavaSourceFileModel getOriginalSource();

    /**
     * Contains the original .class file, assuming that it was originally provided in binary form (as a java .class
     * file)
     */
    @Adjacency(label = CLASS_FILE, direction = Direction.OUT)
    FileModel getClassFile();

    /**
     * Contains the original .class file, assuming that it was originally provided in binary form (as a java .class
     * file)
     */
    @Adjacency(label = CLASS_FILE, direction = Direction.OUT)
    FileModel setClassFile(FileModel file);

    /**
     * Gets the {@link JavaMethodModel} by name
     */
    @GremlinGroovy("it.out('" + JAVA_METHOD + "').has('" + JavaMethodModel.METHOD_NAME + "', methodName)")
    Iterable<JavaMethodModel> getMethod(@GremlinParam("methodName") String methodName);

    /**
     * Adds a {@link JavaMethodModel} to this {@link JavaClassModel}
     */
    @Adjacency(label = JAVA_METHOD, direction = Direction.OUT)
    void addJavaMethod(final JavaMethodModel javaMethod);

    /**
     * Adds a {@link JavaMethodModel} to this {@link JavaClassModel}
     */
    @Adjacency(label = JAVA_METHOD, direction = Direction.OUT)
    Iterable<JavaMethodModel> getJavaMethods();

    /**
     * Returns the {@link JavaClassModel}s that this class depends on
     */
    @GremlinGroovy("it.sideEffect{x=it}.out('" + EXTENDS + "', '" + IMPORTS + "', '" + IMPLEMENTS
                + "').dedup().filter{it!=x}")
    Iterable<JavaClassModel> dependsOnJavaClass();

    /**
     * Returns the {@link JavaClassModel}s that depend on this class
     */
    @GremlinGroovy("it.sideEffect{x=it}.in('" + EXTENDS + "', '" + IMPORTS + "', '" + IMPLEMENTS
                + "').dedup().filter{it!=x}")
    Iterable<JavaClassModel> providesForJavaClass();

}
