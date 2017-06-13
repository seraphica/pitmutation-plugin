package org.jenkinsci.plugins.pitmutation;

import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.model.Result;
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
 * @author edward
 */
public class PitBuildAction implements HealthReportingAction, StaplerProxy {

    private static final String ACTION_URL_NAME = "pitmutation";
    private static final String ICON_FILE = "/plugin/pitmutation/donatello.png";


//    private static Logger logger = Logger.getLogger(PitBuildAction.class.getName());

    private final PitReportResultsReader reportResultsReader;
    private final PrintStream buildLogger;

    private final Run<?, ?> owner;
    private Map<String, MutationReport> reports;


    public PitBuildAction(Run<?, ?> build, PrintStream logger) {
        this.owner = build;
        this.buildLogger = logger;
        this.reportResultsReader = new PitReportResultsReader();
    }

    public PitBuildAction getPreviousAction() {
        while (true) {
            Run<?, ?> previousBuild = owner.getPreviousBuild();
            if (previousBuild == null || previousBuild.getResult() == Result.FAILURE) {
                continue;
            }
            PitBuildAction previousPitBuildAction = previousBuild.getAction(PitBuildAction.class);
            if (previousPitBuildAction != null) {
                return previousPitBuildAction;
            }
        }
    }


//    public ProjectMutations getReport() {
//        return new ProjectMutations(this);
//    }

    public synchronized Map<String, MutationReport> getReports() {
        if (reports == null) {
            reports = reportResultsReader.readReports(owner.getRootDir(), buildLogger);
        }
        return reports;
    }

    /**
     * JELLY
     * Getter for property 'previousResult'.
     *
     * @return Value for property 'previousResult'.
     */
    public PitBuildAction getPreviousResult() {
        return getPreviousResult(owner);
    }

    /**
     * Gets the previous {@link PitBuildAction} of the given build.
     */
    static PitBuildAction getPreviousResult(Run<?, ?> start) {
        Run<?, ?> b = start;
        while (true) {
            b = b.getPreviousNotFailedBuild();
            if (b == null) {
                return null;
            }
            assert b.getResult() != Result.FAILURE : "We asked for the previous not failed build";
            PitBuildAction r = b.getAction(PitBuildAction.class);
            if (r != null) {
                return r;
            }
        }
    }


    @Override
    public ProjectMutations getTarget() {
        return new ProjectMutations(this);
    }

    @Override
    public HealthReport getBuildHealth() {
        return new HealthReport((int) getTarget().getMutationStats().getKillPercent(),
                Messages._BuildAction_Description(getTarget().getMutationStats().getKillPercent()));
    }

    @Override
    public String getIconFileName() {
        return ICON_FILE;
    }

    @Override
    public String getDisplayName() {
        return Messages.BuildAction_DisplayName();
    }

    @Override
    public String getUrlName() {
        return ACTION_URL_NAME;
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

        final JFreeChart chart = ChartFactory.createLineChart(null, // chart title
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
