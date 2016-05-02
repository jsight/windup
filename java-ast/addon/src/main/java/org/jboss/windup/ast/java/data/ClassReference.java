package org.jboss.windup.ast.java.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains a name that has been referenced by the Java source file. This can include the qualified name (for example, com.example.data.Foo) as well
 * as information about the reference. Information includes indicating where the reference was found within the file (line, column, and length) as
 * well as how it was used (import, method call, etc).
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 *
 */
public class ClassReference
{
    private final String qualifiedName;
    private final String packageName;
    private final String className;
    private final String methodName;
    private final ResolutionStatus resolutionStatus;
    private final int lineNumber;
    private final int column;
    private final int length;
    private final TypeReferenceLocation location;
    private final String line;

    private ClassReference superclassReference;
    private final List<ClassReference> interfaces = new ArrayList<>();
    private final List<ClassReference> children = new ArrayList<>();

    /**
     * Creates the {@link ClassReference} with the given qualfiedName, location, lineNumber, column, and length.
     */
    public ClassReference(String qualifiedName, String packageName, String className, String methodName, ResolutionStatus resolutionStatus,
                TypeReferenceLocation location, int lineNumber,
                int column, int length, String line)
    {
        this.qualifiedName = qualifiedName;
        this.packageName = packageName;
        this.className = className;
        this.methodName = methodName;
        this.resolutionStatus = resolutionStatus;
        this.location = location;
        this.lineNumber = lineNumber;
        this.column = column;
        this.length = length;
        this.line = line;
    }

    public void setSuperclassReference(ClassReference superClass)
    {
        this.superclassReference = superClass;
    }

    public void addChildReference(ClassReference reference)
    {
        this.children.add(reference);
    }

    public void addInterface(ClassReference iface)
    {
        this.interfaces.add(iface);
    }

    /**
     * Contains the raw text represented by this reference (class names are not resolved).
     */
    public String getLine()
    {
        return line;
    }

    /**
     * Gets the fully qualified name of the Java element that was referenced.
     */
    public String getQualifiedName()
    {
        return qualifiedName;
    }

    /**
     * Gets the package name (eg, com.example.foo).
     */
    public String getPackageName()
    {
        return packageName;
    }

    /**
     * Gets the simple class name (eg, Foo).
     */
    public String getClassName()
    {
        return className;
    }

    /**
     * Gets the method name (eg, println).
     */
    public String getMethodName()
    {
        return methodName;
    }

    /**
     * Gets the line number where this reference was located.
     */
    public int getLineNumber()
    {
        return lineNumber;
    }

    /**
     * Gets the column where this reference was located.
     */
    public int getColumn()
    {
        return column;
    }

    /**
     * Gets the length of the reference.
     */
    public int getLength()
    {
        return length;
    }

    /**
     * The {@link TypeReferenceLocation} indicates how this reference was used within the file. For example, this can indicate whether it was used as
     * an annotation or a method call.
     */
    public TypeReferenceLocation getLocation()
    {
        return location;
    }

    public ResolutionStatus getResolutionStatus()
    {
        return resolutionStatus;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ClassReference reference = (ClassReference) o;

        if (lineNumber != reference.lineNumber)
            return false;
        if (column != reference.column)
            return false;
        if (length != reference.length)
            return false;
        if (qualifiedName != null ? !qualifiedName.equals(reference.qualifiedName) : reference.qualifiedName != null)
            return false;
        if (resolutionStatus != reference.resolutionStatus)
            return false;
        if (location != reference.location)
            return false;
        return !(line != null ? !line.equals(reference.line) : reference.line != null);

    }

    @Override
    public int hashCode()
    {
        int result = qualifiedName != null ? qualifiedName.hashCode() : 0;
        result = 31 * result + (resolutionStatus != null ? resolutionStatus.hashCode() : 0);
        result = 31 * result + lineNumber;
        result = 31 * result + column;
        result = 31 * result + length;
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (line != null ? line.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "ClassReference{" +
                    "qualifiedName='" + qualifiedName + '\'' +
                    ", resolutionStatus=" + resolutionStatus +
                    ", lineNumber=" + lineNumber +
                    ", column=" + column +
                    ", length=" + length +
                    ", location=" + location +
                    ", line='" + line + '\'' +
                    '}';
    }
}
