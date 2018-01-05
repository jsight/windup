package org.jboss.windup.reporting.rules.generation.techreport;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.tags.Tag;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.model.TagModel;
import org.jboss.windup.reporting.model.TechnologyUsageStatisticsModel;
import org.jboss.windup.reporting.service.TagGraphService;
import org.jboss.windup.util.exception.WindupException;

public class TechReportService
{
    private static final Logger LOG = Logger.getLogger(TechReportService.class.getName());
    public static final String MAPPING_OF_PLACEMENT_NAMES = "techReport:mappingOfPlacementTagNames";

    private final GraphContext graphContext;

    public TechReportService(GraphContext graphContext)
    {
        this.graphContext = graphContext;
    }

    /**
     * Prepares a precomputed matrix - map of maps of maps: rowTag -> boxTag -> project -> placement label -> TechUsageStatSum.
     *
     * @param onlyForProject Sum the statistics only for this project.
     */
    TechStatsMatrix getTechStatsMap(ProjectModel onlyForProject)
    {
        final Long onlyID = onlyForProject == null ? null : (Long) onlyForProject.getRootProjectModel().getElement().id();
        LOG.fine(String.format("### Creating tech stats map for " + (onlyForProject == null ? "global report" : "project #%d"), onlyID));

        Map<String, Map<String, Map<Long, Map<String, TechUsageStatSum>>>> map = new HashMap<>();

        final Iterable<TechnologyUsageStatisticsModel> statModels = graphContext.service(TechnologyUsageStatisticsModel.class).findAll();
        for (TechnologyUsageStatisticsModel stat : statModels)
        {
            List<Long> appsToCountTowards = StreamSupport.stream(stat.getProjectModel().getApplications().spliterator(), false)
                    .map(ProjectModel::getElement)
                    .map(Vertex::id)
                    .map(Long.class::cast)
                    .collect(Collectors.toList());

            LOG.fine(String.format("--- Adding to projects %s: tech '%s', count: %sx, tags: %s", StringUtils.join(appsToCountTowards, " "),
                    stat.getName(), stat.getOccurrenceCount(), stat.getTags()));

            // A shortcut.
            if (onlyID != null && !appsToCountTowards.contains(onlyID))
            {
                LOG.fine("\t\tThis stat is not for this project, skipping.");
                continue;
            }

            final Set<String> placementTags = TechReportService.getPlacementTags(graphContext, stat.getTags());
            TechReportService.TechReportPlacement placement = TechReportService.processPlaceLabels(graphContext, placementTags);
            if (placement.box == null || placement.row == null)
            {
                LOG.severe(String.format("\tPlacement labels not recognized, placement incomplete: %s; stat: %s", placement, stat));
                continue;
            }

            placement = TechReportService.normalizePlacement(graphContext, placement);

            if (placement.box == null || placement.row == null)
            {
                LOG.severe(String.format("\tPlacement labels not recognized, placement incomplete: %s; stat: %s", placement, stat));
                continue;
            }

            // For boxes report - show each tech in sector, row, box. A sum for all projects.
            mergeToTheRightCell(map, placement.row.getName(), placement.box.getName(), 0L, stat.getName(), stat, false);
            // For the punch card report - maximum count for each box.
            mergeToTheRightCell(map, "", placement.box.getName(), 0L, "", stat, true);

            for (Long appToCountTowards : appsToCountTowards)
            {
                if (onlyID != null && onlyID.equals(appToCountTowards))
                    continue;

                // For boxes report - show each tech in sector, row, box. For individual projects.
                mergeToTheRightCell(map, placement.row.getName(), placement.box.getName(), appToCountTowards, stat.getName(), stat, false);

                // For the punch card report - roll up rows and individual techs.
                mergeToTheRightCell(map, "", placement.box.getName(), appToCountTowards, "", stat, false);
            }
        }


        return new TechStatsMatrix(map);
    }

    private static void mergeToTheRightCell(
                Map<String, Map<String, Map<Long, Map<String, TechUsageStatSum>>>> matrix,
                String rowName,
                String boxName,
                Long projectKey,
                String techLabel,
                TechnologyUsageStatisticsModel stat, boolean maxInsteadOfAdd)
    {
        final Map<String, Map<Long, Map<String, TechUsageStatSum>>> rowAll = matrix.computeIfAbsent(rowName, k -> new HashMap());
        final Map<Long, Map<String, TechUsageStatSum>> boxAll = rowAll.computeIfAbsent(boxName, k -> new HashMap<>());
        final Map<String, TechUsageStatSum> statSum = boxAll.computeIfAbsent(projectKey, k -> new HashMap<>());
        final TechUsageStatSum techUsageStatSum = statSum.computeIfAbsent(techLabel, k -> new TechUsageStatSum(techLabel));
        if (maxInsteadOfAdd)
            techUsageStatSum.max(stat);
        else
            techUsageStatSum.add(stat);
    }

    /**
     * Translates the placement tags (labels) to their normalized real tag counterparts. Returns a 2-item array; index 0 has the normal names, index 1
     * the placement names.
     *
     * Due to bad design, the rules contain column and row titles for the graph, rather than technology tags. To make them fit into the tag system,
     * this translation is needed. See also the "place:..." tags in the report hierarchy definition.
     */
    private static Set<String> getPlacementTags(GraphContext graphContext, Set<String> potentialPlaceTags)
    {
        final TagGraphService tagService = new TagGraphService(graphContext);

        Set<String> placeNames = new HashSet<>();
        potentialPlaceTags.forEach(name -> {
            final TagModel placeTag = tagService.getTagByName("place:" + Tag.normalizeName(name));
            if (null != placeTag)
                placeNames.add(placeTag.getName());
        });
        return placeNames;
    }

    /**
     * From three tagNames, if one is under sectorTag and one under rowTag, returns the remaining one, which is supposedly the a box label. Otherwise,
     * returns null.
     */
    private static TechReportPlacement processPlaceLabels(GraphContext graphContext, Set<String> tagNames)
    {
        TagGraphService tagService = new TagGraphService(graphContext);

        if (tagNames.size() < 3)
            throw new WindupException("There should always be exactly 3 placement labels - row, sector, column/box. It was: " + tagNames);
        if (tagNames.size() > 3)
            LOG.severe("There should always be exactly 3 placement labels - row, sector, column/box. It was: " + tagNames);

        TechReportPlacement placement = new TechReportPlacement();

        final TagModel placeSectorsTag = tagService.getTagByName("techReport:placeSectors");
        final TagModel placeBoxesTag = tagService.getTagByName("techReport:placeBoxes");
        final TagModel placeRowsTag = tagService.getTagByName("techReport:placeRows");

        Set<String> unknownTags = new HashSet<>();
        for (String name : tagNames)
        {
            final TagModel tag = tagService.getTagByName(name);
            if (null == tag)
                continue;

            if (TagGraphService.isTagUnderTagOrSame(tag, placeSectorsTag))
            {
                placement.sector = tag;
            }
            else if (TagGraphService.isTagUnderTagOrSame(tag, placeBoxesTag))
            {
                placement.box = tag;
            }
            else if (TagGraphService.isTagUnderTagOrSame(tag, placeRowsTag))
            {
                placement.row = tag;
            } else
            {
                unknownTags.add(name);
            }
        }
        placement.unknown = unknownTags;

        LOG.fine(String.format("\t\tLabels %s identified as: sector: %s, box: %s, row: %s", tagNames, placement.sector, placement.box,
                    placement.row));
        if (placement.box == null || placement.row == null)
        {
            LOG.severe(String.format(
                        "There should always be exactly 3 placement labels - row, sector, column/box. Found: %s, of which box: %s, row: %s", tagNames,
                        placement.box, placement.row));
        }
        return placement;
    }

    /**
     * This relies on the tag structure in the XML when the place:* mapping tags have exactly one parent outside the place: group, which is the tag
     * they are mapped to.
     */
    private static TechReportPlacement normalizePlacement(GraphContext graphContext, TechReportPlacement placement)
    {
        TagGraphService tagService = new TagGraphService(graphContext);

        final TechReportPlacement normalPlacement = new TechReportPlacement();
        normalPlacement.sector = getNonPlaceParent(tagService, placement.sector);
        normalPlacement.box = getNonPlaceParent(tagService, placement.box);
        normalPlacement.row = getNonPlaceParent(tagService, placement.row);
        return normalPlacement;
    }

    private static TagModel getNonPlaceParent(TagGraphService tagService, TagModel tag)
    {
        if (tag == null)
            return null;

        final TagModel placeRoot = tagService.getTagByName(MAPPING_OF_PLACEMENT_NAMES);

        final Iterator<TagModel> parents = tag.getDesignatedByTags().iterator();
        if (!parents.hasNext())
            throw new WindupException("Tag is not designated by any tags: " + tag);

        TagModel nonPlaceParent = null;
        while (parents.hasNext())
        {
            TagModel parentTag = parents.next();
            if (TagGraphService.isTagUnderTagOrSame(parentTag, placeRoot))
                continue;
            if (nonPlaceParent != null)
                throw new WindupException(
                            String.format("Tag %s has more than one non-placement parent: %s, %s", tag.getName(), nonPlaceParent, parentTag));
            nonPlaceParent = parentTag;
        }

        return nonPlaceParent;
    }

    public static class TechStatsMatrix
    {
        // Prepares a precomputed matrix -
        // map of maps of maps: rowTag -> boxTag -> project -> placement label -> TechUsageStatSum.
        private Map<String, Map<String, Map<Long, Map<String, TechUsageStatSum>>>> map = new HashMap<>();

        public TechStatsMatrix(Map<String, Map<String, Map<Long, Map<String, TechUsageStatSum>>>> map)
        {
            this.map = map;
        }

        public TechStatsMatrix()
        {
        }

        public int getMaxForBox(String boxTagName)
        {
            // Data not in a row is stored with the key ""
            final Map<String, Map<Long, Map<String, TechUsageStatSum>>> rowMap = map.get("");

            if (null == rowMap)
                return 0;

            // mergeToTheRightCell(map, "", placement.box.getName(), appToCountTowards, "", stat, false);
            final Map<Long, Map<String, TechUsageStatSum>> boxMap = rowMap.get(boxTagName);
            if (null == boxMap)
                return 0;

            int max = 0;
            for (Map.Entry<Long, Map<String, TechUsageStatSum>> boxEntry : boxMap.entrySet())
            {
                for (Map.Entry<String, TechUsageStatSum> technologyEntry : boxEntry.getValue().entrySet())
                {
                    max = Math.max(max, technologyEntry.getValue().count);
                }
            }
            return max;
        }

        public Map<String, TechUsageStatSum> get(String rowTagName, String boxTagName, Long projectId)
        {
            final Map<String, Map<Long, Map<String, TechUsageStatSum>>> rowMap = map.get(rowTagName);
            if (null == rowMap)
                return null;
            final Map<Long, Map<String, TechUsageStatSum>> boxMap = rowMap.get(boxTagName);
            if (null == boxMap)
                return null;
            return boxMap.get(projectId);
        }

        public TechUsageStatSum get(String rowTagName, String boxTagName, Long projectId, String technology)
        {
            Map<String, TechUsageStatSum> byProject = get(rowTagName, boxTagName, projectId);
            if (null == byProject)
                return null;

            return byProject.get(technology);
        }
    }

    /**
     * A placement of a technology in a tech report. Boxes in grid report == columns in punch card report.
     */
    public static class TechReportPlacement
    {
        TagModel sector;
        TagModel box;
        TagModel row;
        Set<String> unknown;

        @Override
        public String toString()
        {
            return "TechReportPlacement{sector=" + sector + ", box=" + box + ", row=" + row + ", unknown=" + unknown + '}';
        }
    }

    /**
     * Keeps the aggregated data from multiple {@link TechnologyUsageStatisticsModel}s.
     */
    public static class TechUsageStatSum
    {
        String name;
        int count = 0;
        Set<String> tags = new HashSet<>();

        public TechUsageStatSum(String name)
        {
            this.name = name;
        }

        public TechUsageStatSum max(TechnologyUsageStatisticsModel stat)
        {
            this.count = Math.max(count, stat.getOccurrenceCount());
            return this;
        }

        public TechUsageStatSum add(TechnologyUsageStatisticsModel stat)
        {
            if (!"".equals(this.name) && !this.name.equals(stat.getName()))
                throw new IllegalArgumentException("Can't add up stats, " + this.name + " != " + stat.getName());
            this.count += stat.getOccurrenceCount();
            this.tags.addAll(stat.getTags());
            return this;
        }

        public TechUsageStatSum add(TechUsageStatSum stat)
        {
            if (!"".equals(this.name) && !this.name.equals(stat.getName()))
                throw new IllegalArgumentException("Can't add up stats, " + this.name + " != " + stat.getName());
            this.count += stat.getOccurrenceCount();
            this.tags.addAll(stat.getTags());
            return this;
        }

        public String getName()
        {
            return name;
        }

        public int getOccurrenceCount()
        {
            return count;
        }

        public Set<String> getTags()
        {
            return tags;
        }

        @Override
        public String toString()
        {
            return "{" + name + " " + count + "×, [" + tags + "]}";
        }
    }
}
