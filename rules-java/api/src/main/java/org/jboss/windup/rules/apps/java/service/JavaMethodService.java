package org.jboss.windup.rules.apps.java.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.JavaMethodModel;

public class JavaMethodService extends GraphService<JavaMethodModel>
{
    private final JavaParameterService paramService;

    public JavaMethodService(GraphContext context)
    {
        super(context, JavaMethodModel.class);
        this.paramService = new JavaParameterService(context);
    }
}
