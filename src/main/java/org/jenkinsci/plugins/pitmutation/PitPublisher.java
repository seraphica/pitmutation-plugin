package org.jenkinsci.plugins.pitmutation;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.pitmutation.targets.MutationStats;
import org.jenkinsci.plugins.pitmutation.targets.ProjectMutations;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Pit publisher.
 *
 * @author edward
 */
public class PitPublisher extends Recorder implements SimpleBuildStep {


    private static final String ROOT_REPORT_FOLDER = "mutation-report";
    private static final String DEFAULT_MODULE_NAME = "module";
    private static final String DEFAULT_MUTATION_STATS_FILE = "**/mutations.xml";
    /**
     * The constant DESCRIPTOR.
     */
    @Extension
    public static final BuildStepDescriptor<Publisher> DESCRIPTOR = new DescriptorImpl();

    private List<Condition> buildConditions;
    private String mutationStatsFile;
    private boolean killRatioMustImprove;
    private float minimumKillRatio;
    private transient TaskListener listener;
    private Run<?, ?> build;

    /**
     * Instantiates a new Pit publisher.
     *
     * @param mutationStatsFile    the mutation stats file
     * @param minimumKillRatio     the minimum kill ratio
     * @param killRatioMustImprove the kill ratio must improve
     */
    @DataBoundConstructor
    public PitPublisher(String mutationStatsFile, float minimumKillRatio, boolean killRatioMustImprove) {
        this.mutationStatsFile = StringUtils.isEmpty(mutationStatsFile) ? DEFAULT_MUTATION_STATS_FILE : mutationStatsFile;
        this.killRatioMustImprove = killRatioMustImprove;
        this.minimumKillRatio = minimumKillRatio;
        this.buildConditions = new ArrayList<Condition>();
        this.buildConditions.add(percentageThreshold(minimumKillRatio));
        if (killRatioMustImprove) {
            this.buildConditions.add(mustImprove());
        }
    }

    @Override
    public void perform(@Nonnull Run<?, ?> build, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws InterruptedException, IOException {
        this.listener = listener;
        this.build = build;
        this.listener.getLogger().println("Looking for PIT reports in " + workspace.getRemote());

        ParseReportCallable fileCallable = new ParseReportCallable(mutationStatsFile);
        FilePath[] reports = workspace.act(fileCallable); //znajdz raporty po patternie w katalogu workspace
        publishReports(reports, new FilePath(build.getRootDir())); //skopiuj do mutation-report

        PitBuildAction action = new PitBuildAction(build, listener.getLogger());
        build.addAction(action);
        build.setResult(decideBuildResult(action));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new PitProjectAction(project);
    }

    /**
     * Publish reports.
     *
     * @param reports     the reports
     * @param buildTarget the build target
     */
    void publishReports(FilePath[] reports, FilePath buildTarget) {
        if (isMultiModuleProject(reports)) {
            copyMultiModulesReportsFiles(reports, buildTarget);
        } else {
            copySingleModuleReportFiles(reports, buildTarget);
        }
    }

    private void copySingleModuleReportFiles(FilePath[] reports, FilePath buildTarget) {
        if (reports.length > 0) {
            copyMutationReport(reports[0], buildTarget, ROOT_REPORT_FOLDER + File.separator + DEFAULT_MODULE_NAME);//FIXME single module strategy
        }
    }

    private void copyMultiModulesReportsFiles(FilePath[] reports, FilePath buildTarget) {
        for (int i = 0; i < reports.length; i++) {
            copyMutationReport(reports[i], buildTarget, createReportPathForMultiModule(reports[i].getRemote()));
        }
    }

    private boolean isMultiModuleProject(FilePath[] reports) {
        return reports.length > 1;
    }

    private void copyMutationReport(FilePath report, FilePath buildTarget, String reportPath) {
        listener.getLogger().println("Publishing mutation report: " + report.getRemote());
        try {
            report.getParent().copyRecursiveTo(new FilePath(buildTarget, reportPath));
        } catch (IOException e) {
            Util.displayIOException(e, listener);
            e.printStackTrace(listener.fatalError("Unable to copy coverage from " + report + " to " + buildTarget));
            build.setResult(Result.FAILURE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String createReportPathForMultiModule(String remote) {
        return getAggregatorFolderName() + File.separator + extractModuleName(remote);
    }

    /**
     * Mutations report exists boolean.
     *
     * @param reportDir the report dir
     * @return the boolean
     */
    boolean mutationsReportExists(FilePath reportDir) {
        if (reportDir == null) {
            return false;
        }
        try {
            FilePath[] search = reportDir.list("**/mutations.xml");
            return search.length > 0;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * Decide build result result.
     *
     * @param action the action
     * @return the worst result from all conditions
     */
    public Result decideBuildResult(PitBuildAction action) {
        Result result = Result.SUCCESS;
        for (Condition condition : buildConditions) {
            Result conditionResult = condition.decideResult(action);
            result = conditionResult.isWorseThan(result) ? conditionResult : result;
        }
        return result;
    }


    /**
     * JELLY
     * Required by plugin config
     *
     * @return the minimum kill ratio
     */
    public float getMinimumKillRatio() {
        return minimumKillRatio;
    }

    /**
     * JELLY
     * Required by plugin config
     *
     * @return the kill ratio must improve
     */
    public boolean getKillRatioMustImprove() {
        return killRatioMustImprove;
    }

    /**
     * JELLY
     * Required by plugin config
     *
     * @return the mutation stats file
     */
    public String getMutationStatsFile() {
        return mutationStatsFile;
    }

    private Condition percentageThreshold(final float percentage) {
        return new Condition() {
            public Result decideResult(PitBuildAction action) {
                MutationStats stats = new ProjectMutations(action).getMutationStats();
                listener.getLogger().println("Kill ratio is " + stats.getKillPercent() + "% ("
                        + stats.getKillCount() + "  " + stats.getTotalMutations() + ")");
                return stats.getKillPercent() >= percentage ? Result.SUCCESS : Result.FAILURE;
            }
        };
    }

    private Condition mustImprove() {
        return new Condition() {
            public Result decideResult(final PitBuildAction action) {
                PitBuildAction previousAction = action.getPreviousAction();
                if (previousAction != null) {
                    MutationStats stats = new ProjectMutations(action).getMutationStats();
                    listener.getLogger().println("Previous kill ratio was " + stats.getKillPercent() + "%");
                    return new ProjectMutations(action).getMutationStats().getKillPercent() <= stats.getKillPercent()
                            ? Result.SUCCESS : Result.UNSTABLE;
                } else {
                    return Result.SUCCESS;
                }
            }
        };
    }

    @Override
    public BuildStepDescriptor<Publisher> getDescriptor() {
        return DESCRIPTOR;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    private FilePath getReportDir(FilePath root) throws IOException, InterruptedException {
        FilePath reportsDir = new FilePath(root, mutationStatsFile);
        if (reportsDir.isDirectory()) {
            return reportsDir;
        } else {
            return reportsDir.getParent();
        }
    }


    String extractModuleName(String workspacePathToMutationReport) {
        String partialSubstring = StringUtils.substringBefore(workspacePathToMutationReport, File.separator + "target" + File.separator);
        return StringUtils.substringAfterLast(partialSubstring, File.separator);
    }

    public String getAggregatorFolderName() {
        return ROOT_REPORT_FOLDER;
    }

    /**
     * The type Descriptor.
     */
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        /**
         * Instantiates a new Descriptor.
         */
        public DescriptorImpl() {
            super(PitPublisher.class);
        }

        @Override
        public String getDisplayName() {
            return Messages.PitPublisher_DisplayName();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            req.bindParameters(this, "pitmutation");
            save();
            return super.configure(req, formData);
        }

        /**
         * Creates a new instance of {@link PitPublisher} from a submitted form.
         */
        @Override
        public PitPublisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            PitPublisher instance = req.bindJSON(PitPublisher.class, formData);
            return instance;
        }
    }

    /**
     * The type Parse report callable.
     */
    public static class ParseReportCallable implements FilePath.FileCallable<FilePath[]> {

        private static final long serialVersionUID = 1L;

        private final String reportFilePath;

        /**
         * Instantiates a new Parse report callable.
         *
         * @param reportFilePath the report file path
         */
        public ParseReportCallable(String reportFilePath) {
            this.reportFilePath = reportFilePath;
        }

        public FilePath[] invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
            FilePath[] r = new FilePath(f).list(reportFilePath);
            if (r.length < 1) {
                throw new IOException("No reports found at location:" + reportFilePath);
            }
            return r;
        }

        @Override
        public void checkRoles(RoleChecker roleChecker) throws SecurityException {

        }
    }
}
