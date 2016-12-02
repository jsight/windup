package org.jboss.windup.tooling;

import java.rmi.RemoteException;
import java.util.Collection;

/**
 * Allows configuring options on Windup.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface ExecutionBuilderSetOptions
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
     * Includes the provided list of package prefixes.
     */
	void includePackages(Collection<String> includePackagePrefixes) throws RemoteException;

    /**
     * Sets the package name prefixes to ignore.
     */
	void excludePackage(String excludePackagePrefix) throws RemoteException;

    /**
     * Sets a list of package name prefixes to ignore.
     */
	void excludePackages(Collection<String> excludePackagePrefixes) throws RemoteException;

    /**
     * Switches the engine to run in source only mode (no decompilation).
     */
	void sourceOnlyMode() throws RemoteException;

    /**
     * Indicates that Windup should not generate reports at the end.
     */
	void skipReportGeneration() throws RemoteException;

    /**
     * Adds a custom uer rules path.
     */
	void addUserRulesPath(String rulesPath) throws RemoteException;

    /**
     * Adds a set of custom uer rules paths.
     */
	void addUserRulesPaths(Iterable<String> rulesPath) throws RemoteException;

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
