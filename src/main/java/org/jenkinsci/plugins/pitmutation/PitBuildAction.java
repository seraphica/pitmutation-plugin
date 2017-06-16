package org.jenkinsci.plugins.pitmutation;

import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.model.Run;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import org.jenkinsci.plugins.pitmutation.targets.ProjectMutations;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Map;

/**
 * main plugin action serialized into build.xml of jobs build
 *
 * @author edward
 */
public class PitBuildAction implements HealthReportingAction, StaplerProxy {

    private static final String URL_NAME = "pitmutation";

    private final PrintStream buildLogger;
    private ProjectMutations projectMutations;

    private final Run<?, ?> owner;

    public PitBuildAction(Run<?, ?> build, PrintStream logger) {
        this.owner = build;
        this.buildLogger = logger;
    }


    public ProjectMutations getProjectMutations() {
        if (projectMutations == null) {
            projectMutations = new ProjectMutations(this);
        }
        return projectMutations;
    }

    public synchronized Map<String, MutationReport> getReports() {
        return projectMutations.getReports();
    }

    public PrintStream getBuildLogger() {
        return buildLogger;
    }

    /**
     * JELLY
     * Getter for property 'previousResult'.
     *
     * @return Value for property 'previousResult'.
     */
    public PitBuildAction getPreviousResult() {
        return getPreviousNotFailedBuildAction();
    }
    //        return new ProjectMutations(this);

    public PitBuildAction getPreviousAction() {
        return getPreviousNotFailedBuildAction();
    }

    /**
     * Gets the previous {@link PitBuildAction} of the given build.
     */
    private PitBuildAction getPreviousNotFailedBuildAction() {
        Run<?, ?> build = owner.getPreviousNotFailedBuild();
        while (build != null) {
            build = build.getPreviousNotFailedBuild();
            if (build == null) {
                return null;
            }
            return build.getAction(PitBuildAction.class);
        }
        return null;
    }


    @Override
    public ProjectMutations getTarget() {
        return getProjectMutations();
    }

    @Override
    public HealthReport getBuildHealth() { //FIXME WTF???????????????????????
//        return new HealthReport((int) getProjectMutations().getMutationStats().getKillPercent(),
//                Messages._PitBuildAction_Description(getProjectMutations().getMutationStats().getKillPercent()));
        return null;
    }

    @Override
    public String getIconFileName() {
        return Messages.PitBuildAction_IconFileName();
    }

    @Override
    public String getDisplayName() {
        return Messages.PitBuildAction_DisplayName();
    }

    @Override
    public String getUrlName() {
        return URL_NAME;
    }

    //JELLY
    public Run<?, ?> getOwner() {
        return owner;
    }

    /**
     * STAPLER
     * Generates the graph that shows the coverage trend up to this report.
     */
    public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        buildLogger.println("Probuje wykres narysowac");
        if (ChartUtil.awtProblemCause != null) {
            // not available. send out error message
            rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
            return;
        }

        buildLogger.println("Dalej Probuje wykres narysowac");
        Calendar t = owner.getTimestamp();

        if (req.checkIfModified(t, rsp)) {
            return; // up to date
        }
        DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

        buildLogger.println("I Dalej Probuje wykres narysowac");

        final JFreeChart chart = ChartFactory.createLineChart("tutaj sobie testuje", // chart title
                null, // unused
                "%", // range axis label
                dsb.build(), // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                true, // tooltips
                false // urls
        );//    JFreeChart chart = new MutationChart(this).createChart();
        ChartUtil.generateGraph(req, rsp, chart, 500, 200);
    }

}
