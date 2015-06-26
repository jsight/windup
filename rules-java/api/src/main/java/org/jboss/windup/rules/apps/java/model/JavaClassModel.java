package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents a JavaClass, either from a .class file or a .java source file.
 * 
 */
@TypeValue(JavaClassModel.TYPE)
public interface JavaClassModel extends WindupVertexFrame
{
    String TYPE = "JavaClassResource";
    String CLASS_FILE = "classFile";
    String ORIGINAL_SOURCE = "originalSource";
    String DECOMPILED_SOURCE = "decompiledSource";
    String QUALIFIED_NAME = "qualifiedName";
    String CLASS_NAME = "className";
    String PACKAGE_NAME = "packageName";
    String IS_PUBLIC = "isPublic";

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
     * Contains the {@link JavaSourceFileModel} of the decompiled version of this file (assuming that it originally was
     * decompiled from a .class file)
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
}
