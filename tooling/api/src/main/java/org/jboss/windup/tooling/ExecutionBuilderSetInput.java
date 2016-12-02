package org.jboss.windup.tooling;

import java.rmi.RemoteException;

/**
 * Allows setting the input path.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface ExecutionBuilderSetInput
{
    /**
     * Sets the input path (application source directory, or application binary file).
     */
	void setInput(String input) throws RemoteException;
}
