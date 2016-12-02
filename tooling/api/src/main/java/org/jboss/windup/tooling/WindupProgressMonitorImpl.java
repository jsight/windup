package org.jboss.windup.tooling;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class WindupProgressMonitorImpl extends UnicastRemoteObject implements Remote, WindupToolingProgressMonitor {

	private static final long serialVersionUID = 2095906104816131435L;
	
	private WindupToolingProgressMonitor delegate;
	
	public WindupProgressMonitorImpl(WindupToolingProgressMonitor delegate) throws RemoteException {
		super();
		this.delegate = delegate;
	}

	@Override
	public void beginTask(String name, int totalWork) throws RemoteException {
	}

	@Override
	public void done() throws RemoteException {
	}

	@Override
	public boolean isCancelled() throws RemoteException {
		return false;
	}

	@Override
	public void setCancelled(boolean value) throws RemoteException {
	}

	@Override
	public void setTaskName(String name) throws RemoteException {
	}

	@Override
	public void subTask(String name) throws RemoteException {
	}

	@Override
	public void worked(int work) throws RemoteException {
	}
}
