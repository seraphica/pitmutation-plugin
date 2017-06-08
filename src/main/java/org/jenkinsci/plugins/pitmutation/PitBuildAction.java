package org.jenkinsci.plugins.pitmutation;

import hudson.FilePath;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.model.Result;
import hudson.model.Run;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.pitmutation.targets.ProjectMutations;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author edward
 */
public class PitBuildAction implements HealthReportingAction, StaplerProxy {


    private static Logger logger = Logger.getLogger(PitBuildAction.class.getName());

    private static final String ROOT_REPORT_FOLDER = "mutation-report";
    private PrintStream buildLogger;

    private Run<?, ?> owner;
    private Map<String, MutationReport> reports;


    public PitBuildAction(Run<?, ?> build, PrintStream logger) {
        this.owner = build;
        this.buildLogger = logger;
    }

    public PitBuildAction getPreviousAction() {
        Run<?, ?> b = owner;
        while (true) {
            b = b.getPreviousBuild();
            if (b == null) {
                return null;
            }
            if (b.getResult() == Result.FAILURE) {
                continue;
            }
            PitBuildAction r = b.getAction(PitBuildAction.class);
            if (r != null) {
                return r;
            }
        }
    }

    public Run<?, ?> getOwner() {
        return owner;
    }

    public ProjectMutations getTarget() {
        return getReport();
    }

    public ProjectMutations getReport() {
        return new ProjectMutations(this);
    }

    public synchronized Map<String, MutationReport> getReports() {
        if (reports == null) {
            reports = readReports();
        }
        return reports;
    }

    private Map<String, MutationReport> readReports() {
        Map<String, MutationReport> reports = new HashMap<String, MutationReport>();

        try {
            FilePath[] files = new FilePath(owner.getRootDir()).list(ROOT_REPORT_FOLDER + "/**/mutations.xml");
            buildLogger.println("filesów znaleziono " + files.length);
            if (files.length < 1) {
                buildLogger.println("Could not find " + ROOT_REPORT_FOLDER + "/**/mutations.xml in " + owner.getRootDir());
            }
            for (int i = 0; i < files.length; i++) {
                if (files.length == 1) {
                    buildLogger.println(" >>> Creating report for file: " + files[i].getRemote());
                    reports.put(ROOT_REPORT_FOLDER, MutationReport.create(files[i].read()));
                } else {
                    buildLogger.println("Creating report for file: " + files[i].getRemote());
                    reports.put(extractModuleName(files[i].getRemote()), MutationReport.create(files[i].read()));
                }
            }
        } catch (IOException e) {
            buildLogger.println("IO EXCEPTION");
            e.printStackTrace();
        } catch (SAXException e) {
            buildLogger.println("SAX EXCEPTION");
            e.printStackTrace();
        } catch (InterruptedException e) {
            buildLogger.println("Interrupted EXCEPTION");
            e.printStackTrace();
        }
        buildLogger.println(" ****** Reports siezer found" + reports.size());
        logger.log(Level.WARNING, "Reports siezer found" + reports.size());
        return reports;
    }

    String extractModuleName(String remote) { //FIXME if single module returns empty string
        String pathToMutationReport = StringUtils.substringBeforeLast(remote, File.separator);
        return StringUtils.substringAfterLast(pathToMutationReport, File.separator + ROOT_REPORT_FOLDER + File.separator);
    }

    /*
    void publishReports(FilePath[] reports, FilePath buildTarget) {
        if (isMultiModuleProject(reports)) {
            copyMultiModulesReportsFiles(reports, buildTarget);
        } else {
            copySingleModuleReportFiles(reports, buildTarget);
        }
    }
     */

    /**
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

    public HealthReport getBuildHealth() {
        return new HealthReport((int) getReport().getMutationStats().getKillPercent(),
                Messages._BuildAction_Description(getReport().getMutationStats().getKillPercent()));
    }

    public String getIconFileName() {
        return "/plugin/pitmutation/donatello.png";
    }

    public String getDisplayName() {
        return Messages.BuildAction_DisplayName();
    }

    public String getUrlName() {
        return "pitmutation";
    }

    /**
     * Generates the graph that shows the coverage trend up to this report.
     */
    public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if (ChartUtil.awtProblemCause != null) {
            // not available. send out error message
            rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
            return;
        }

        Calendar t = owner.getTimestamp();

        if (req.checkIfModified(t, rsp)) {
            return; // up to date
        }
        DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();


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
