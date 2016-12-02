package org.jboss.windup.tooling;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class WindupProgressLoggingHandler extends Handler {
	 private final WindupToolingProgressMonitor monitor;

     public WindupProgressLoggingHandler(WindupToolingProgressMonitor monitor)
     {
         this.monitor = monitor;
     }

     @Override
     public void publish(LogRecord record)
     {
         if (this.monitor == null)
             return;

         //this.monitor.logMessage(record);
     }

     @Override
     public void flush()
     {

     }

     @Override
     public void close() throws SecurityException
     {

     }
}
