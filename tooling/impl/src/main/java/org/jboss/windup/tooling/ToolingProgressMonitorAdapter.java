package org.jboss.windup.tooling;

import java.util.logging.LogRecord;

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

    @Override
    public void logMessage(LogRecord logRecord)
    {
        this.delegate.logMessage(logRecord);
    }

    @Override
    public void beginTask(String name, int totalWork)
    {
        this.delegate.beginTask(name, totalWork);
    }

    @Override
    public void done()
    {
        this.delegate.done();
    }

    @Override
    public boolean isCancelled()
    {
        return this.delegate.isCancelled();
    }

    @Override
    public void setCancelled(boolean value)
    {
        this.delegate.setCancelled(value);
    }

    @Override
    public void setTaskName(String name)
    {
        this.delegate.setTaskName(name);
    }

    @Override
    public void subTask(String name)
    {
        this.delegate.subTask(name);
    }

    @Override
    public void worked(int work)
    {
        this.delegate.worked(work);
    }
}
