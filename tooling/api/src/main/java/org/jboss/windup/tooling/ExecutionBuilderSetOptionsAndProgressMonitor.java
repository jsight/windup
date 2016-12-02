package org.jboss.windup.tooling;

import java.rmi.RemoteException;

/**
 * Allows setting windup options, including the {@link WindupProgressMonitor}.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface ExecutionBuilderSetOptionsAndProgressMonitor
{
    /**
     * Sets a pattern of file paths to ignore during processing.
     */
	void ignore(String ignorePattern) throws RemoteException;

    /**
     * Sets the package name prefixes to scan (the default is to scan all packages).
     */
	void includePackage(String includePackagePrefix) throws RemoteException;

    /**
     * Sets the package name prefixes to ignore.
     */
	void excludePackage(String excludePackagePrefix) throws RemoteException;

    /**
     * Sets the callback that will be used for monitoring progress.
     */
	void setProgressMonitor(WindupToolingProgressMonitor monitor) throws RemoteException;

    /**
     * Sets the option with the specified name to the specified value. Option names can be found in static variables on {@link ConfigurationOption}
     * implementations.
     */
	void setOption(String name, Object value) throws RemoteException;

    /**
     * Execute windup.
     */
    ExecutionResults execute() throws RemoteException;
}
