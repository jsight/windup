package org.jboss.windup.tooling;

import java.rmi.RemoteException;

import org.jboss.windup.exec.WindupProgressMonitor;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ToolingProgressMonitorAdapter implements WindupToolingProgressMonitor, WindupProgressMonitor
{
    private final WindupToolingProgressMonitor delegate;

    public ToolingProgressMonitorAdapter(WindupToolingProgressMonitor delegate)
    {
        this.delegate = delegate;
    }

    /*@Override
    public void logMessage(LogRecord logRecord)
    {
        this.delegate.logMessage(logRecord);
    }*/

    @Override
    public void beginTask(String name, int totalWork)  
    {
        try {
			this.delegate.beginTask(name, totalWork);
		} catch (RemoteException e) {
		}
    }

    @Override
    public void done()
    {
        try {
			this.delegate.done();
		} catch (RemoteException e) {
		}
    }

    @Override
    public boolean isCancelled()
    {
        try {
			return this.delegate.isCancelled();
		} catch (RemoteException e) {
			return true;
		}
    }

    @Override
    public void setCancelled(boolean value)
    {
        try {
			this.delegate.setCancelled(value);
		} catch (RemoteException e) {
		}
    }

    @Override
    public void setTaskName(String name)
    {
        try {
			this.delegate.setTaskName(name);
		} catch (RemoteException e) {
		}
    }

    @Override
    public void subTask(String name)
    {
        try {
			this.delegate.subTask(name);
		} catch (RemoteException e) {
		}
    }

    @Override
    public void worked(int work)
    {
        try {
			this.delegate.worked(work);
		} catch (RemoteException e) {
		}
    }
}
