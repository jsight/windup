package org.jboss.windup.tooling.data;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface IssueCategory
{
    String getCategoryID();
    String getOrigin();
    String getName();
    String getDescription();
    Integer getPriority();
}
