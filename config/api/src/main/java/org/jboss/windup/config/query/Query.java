package org.jboss.windup.config.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Iterables;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.selectors.FramesSelector;
import org.jboss.windup.graph.GraphTypeManager;
import org.jboss.windup.graph.frames.VertexFromFramedIterable;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.Task;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.frames.FramedGraphQuery;
import com.tinkerpop.frames.structures.FramedVertexIterable;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;

public class Query extends GraphCondition implements QueryBuilderFind, QueryBuilderFrom, QueryBuilderWith,
            QueryBuilderPiped
{
    private String outputVar = Iteration.DEFAULT_VARIABLE_LIST_STRING;

    private final List<QueryGremlinCriterion> pipelineCriteria = new ArrayList<>();

    private Class<? extends WindupVertexFrame> searchType;

    private FramesSelector framesSelector;

    private Predicate<WindupVertexFrame> resultFilter;

    private Query()
    {
    }

    /**
     * Begin this {@link Query} with all Frame instances that are the result of the provided GremlinQueryCriterion.
     */
    public static QueryBuilderPiped gremlin(final QueryGremlinCriterion criterion)
    {
        return new Query().piped(criterion);
    }

    /**
     * Begin this {@link Query} with all {@link WindupVertexFrame} instances of the given type.
     */
    public static QueryBuilderFind fromType(Class<? extends WindupVertexFrame> type)
    {
        final Query query = new Query();
        // this query is going to be added after evaluate() method, because in some cases we need gremlin and in some
        // frames
        query.searchType = type;
        return query;
    }

    /**
     * Excludes Vertices that are of the provided type.
     */
    @Override
    public QueryBuilderFind excludingType(final Class<? extends WindupVertexFrame> type)
    {
        pipelineCriteria.add(new QueryGremlinCriterion()
        {
            @Override
            public void query(GraphRewrite event, GremlinPipeline<Vertex, Vertex> pipeline)
            {
                pipeline.filter(new PipeFunction<Vertex, Boolean>()
                {
                    @Override
                    public Boolean compute(Vertex argument)
                    {
                        return !GraphTypeManager.hasType(type, argument);
                    }
                });
            }
        });
        return this;
    }

    /**
     * Includes Vertices that are of the provided type.
     */
    @Override
    public QueryBuilderFind includingType(final Class<? extends WindupVertexFrame> type)
    {
        pipelineCriteria.add(new QueryGremlinCriterion()
        {
            @Override
            public void query(GraphRewrite event, GremlinPipeline<Vertex, Vertex> pipeline)
            {
                pipeline.filter(new PipeFunction<Vertex, Boolean>()
                {
                    @Override
                    public Boolean compute(Vertex argument)
                    {
                        return GraphTypeManager.hasType(type, argument);
                    }
                });
            }
        });
        return this;
    }

    /**
     * Begin this {@link Query} with results of a prior {@link Query}, read from the variable with the given name.
     */
    public static QueryBuilderFrom from(final String sourceVarName)
    {
        final Query query = new Query();
        query.setInputVariablesName(sourceVarName);
        return query;
    }

    @Override
    public ConditionBuilder as(String outputVarName)
    {
        outputVar = outputVarName;
        return this;
    }

    /*
     * Evaluators
     */

    @Override
    public boolean evaluate(final GraphRewrite event, final EvaluationContext context)
    {
        final String queryStr = toString();
        return ExecutionStatistics.performBenchmarked(queryStr, new Task<Boolean>()
        {
            public Boolean execute()
            {
                Query.this.setInitialFramesSelector(createInitialFramesSelector(Query.this));
                Iterable<? extends WindupVertexFrame> result = framesSelector.getFrames(event, context);
                if (resultFilter != null)
                {
                    com.google.common.base.Predicate<WindupVertexFrame> guavaPred= new com.google.common.base.Predicate<WindupVertexFrame>() {
                        @Override public boolean apply(WindupVertexFrame input)
                        {
                            return resultFilter.accept(input);
                        }
                    };
                    result = Iterables.filter(result, guavaPred);
                }

                setResults(event, outputVar, result);
                return result.iterator().hasNext();
            }
        });
    }

    /*
     * Criteria
     */

    @Override
    public QueryBuilderWith withProperty(String property, Object searchValue)
    {
        return withProperty(property, QueryPropertyComparisonType.EQUALS, searchValue);
    }

    @Override
    public QueryBuilderWith withProperty(String property, Iterable<?> values)
    {
        pipelineCriteria.add(new QueryPropertyCriterion(property, QueryPropertyComparisonType.CONTAINS_ANY_TOKEN, values));
        return this;
    }

    @Override
    public QueryBuilderWith withProperty(String property)
    {
        pipelineCriteria.add(new QueryPropertyCriterion(property, QueryPropertyComparisonType.DEFINED, null));
        return this;
    }

    @Override
    public QueryBuilderWith withoutProperty(String property)
    {
        pipelineCriteria.add(new QueryPropertyCriterion(property, QueryPropertyComparisonType.NOT_DEFINED, null));
        return this;
    }

    private static FramesSelector createInitialFramesSelector(final Query query)
    {
        return new FramesSelector()
        {
            @Override
            public Iterable<WindupVertexFrame> getFrames(GraphRewrite event, EvaluationContext context)
            {
                Iterable<Vertex> startingVertices = getStartingVertices(event);
                GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(startingVertices);
                Set<WindupVertexFrame> frames = new HashSet<>();
                for (QueryGremlinCriterion c : query.getPipelineCriteria())
                {
                    c.query(event, pipeline);
                }

                FramedVertexIterable<WindupVertexFrame> framedVertexIterable = new FramedVertexIterable<>(
                            event.getGraphContext().getFramed(), pipeline,
                            WindupVertexFrame.class);
                for (WindupVertexFrame frame : framedVertexIterable)
                {
                    frames.add(frame);
                }
                return frames;
            }

            private Iterable<Vertex> getStartingVertices(GraphRewrite event)
            {
                boolean hasStartingVerticesVariable = query.getInputVariablesName() != null
                            && !query.getInputVariablesName().isEmpty();
                Iterable<Vertex> startingVertices;
                if (hasStartingVerticesVariable)
                {
                    // save the type as a gremlin criterion
                    if (query.searchType != null)
                    {
                        query.piped(new QueryTypeCriterion(query.searchType));
                    }
                    Variables variables = (Variables) event.getRewriteContext().get(Variables.class);
                    Iterable<? extends WindupVertexFrame> frames = variables.findVariable(query.getInputVariablesName());
                    return new VertexFromFramedIterable(frames);
                }
                else
                {
                    FramedGraphQuery framesQueryType = event.getGraphContext().getFramed().query();
                    if (query.searchType != null)
                    {
                        new QueryTypeCriterion(query.searchType).query(framesQueryType);
                        startingVertices = framesQueryType.vertices();
                        return startingVertices;
                    }

                }
                return event.getGraphContext().getGraph().getVertices();
            }
        };
    }

    @Override
    public QueryBuilderWith withProperty(String property, Object searchValue, Object... searchValues)
    {
        List<Object> values = new LinkedList<>();
        values.add(searchValue);
        values.addAll(Arrays.asList(searchValues));

        return withProperty(property, values);
    }

    @Override
    public QueryBuilderWith withProperty(String property, QueryPropertyComparisonType searchType,
                Object searchValue)
    {
        pipelineCriteria.add(new QueryPropertyCriterion(property, searchType, searchValue));
        return this;
    }

    @Override
    public QueryBuilderPiped piped(QueryGremlinCriterion criterion)
    {
        pipelineCriteria.add(criterion);
        return this;
    }

    private void setInitialFramesSelector(FramesSelector selector)
    {
        this.framesSelector = selector;
    }

    public Collection<QueryGremlinCriterion> getPipelineCriteria()
    {
        return pipelineCriteria;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <FRAMETYPE extends WindupVertexFrame> QueryBuilderAs filteredBy(Predicate<FRAMETYPE> predicate)
    {
        this.resultFilter = (Predicate<WindupVertexFrame>) predicate;
        return this;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Query");
        if (searchType != null)
        {
            builder.append(".fromType(").append(searchType.getName()).append(")");
        }

        if (!pipelineCriteria.isEmpty())
        {
            builder.append(".gremlin()");
            for (QueryGremlinCriterion criterion : pipelineCriteria)
            {
                builder.append(criterion);
            }
        }
        builder.append(".as(" + outputVar + ")");
        return builder.toString();
    }

}
