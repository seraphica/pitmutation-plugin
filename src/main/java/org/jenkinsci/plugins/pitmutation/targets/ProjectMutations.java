package org.jenkinsci.plugins.pitmutation.targets;

import com.google.common.collect.Maps;
import hudson.model.Run;
import org.jenkinsci.plugins.pitmutation.Mutation;
import org.jenkinsci.plugins.pitmutation.MutationReport;
import org.jenkinsci.plugins.pitmutation.PitBuildAction;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ed Kimber
 */
public class ProjectMutations extends MutationResult<ProjectMutations> {

    private static final String RESULT_NAME = "aggregate";
    private static final String DISPLAY_NAME = "Modules";
    private static final String NAME = "Aggregated Reports";


    private final PitBuildAction pitBuildAction;
    private ProjectMutations previousResult;

    private Map<String, MutationReport> reports;

    public ProjectMutations(PitBuildAction pitBuildAction) {
        super(RESULT_NAME, null);
        this.pitBuildAction = pitBuildAction;
    }

    @Override
    public Run<?, ?> getOwner() {
        return pitBuildAction.getOwner();
    }

    @Override
    public ProjectMutations getPreviousResult() {
        if (pitBuildAction == null) { //FIXME
            return null;
        }
        if (previousResult == null) {
            previousResult = new ProjectMutations(pitBuildAction.getPreviousAction());
        }
        return previousResult;
    }

    @Override
    public MutationStats getMutationStats() {
        return aggregateStats(getReports().values());
    }

    private static MutationStats aggregateStats(Collection<MutationReport> reports) {
        MutationStats stats = new MutationStatsImpl("", new ArrayList<Mutation>(0));
        for (MutationReport report : reports) {
            stats = stats.aggregate(report.getMutationStats());
        }
        return stats;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public Map<String, ? extends MutationResult<?>> getChildMap() {
        Map<String, MutationReport> reports = getReports();
        if (reports.isEmpty()) {
            return new HashMap<String, MutationResult<?>>();
        }
        return Maps.transformEntries(reports, new Maps.EntryTransformer<String, MutationReport, ModuleResult>() {
            public ModuleResult transformEntry(String moduleName, MutationReport report) {
                return new ModuleResult(moduleName, ProjectMutations.this, report);
            }
        });
    }

    @Override
    public int compareTo(ProjectMutations other) {
        return this.getMutationStats().getUndetected() - other.getMutationStats().getUndetected();
    }

    public synchronized Map<String, MutationReport> getReports() {
        if (pitBuildAction == null) { //FIXME
            return new HashMap<String, MutationReport>();
        }
        if (reports == null) {
            File rootDir = pitBuildAction.getOwner().getRootDir();
            PrintStream buildLogger = pitBuildAction.getBuildLogger();
            reports = new PitReportResultsReader().readReports(rootDir, buildLogger);
        }
        return reports;
    }
}
