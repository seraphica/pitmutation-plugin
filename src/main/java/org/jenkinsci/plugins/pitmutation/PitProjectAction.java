package org.jenkinsci.plugins.pitmutation;

import hudson.model.*;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;

/**
 * @author Ed Kimber
 */
public class PitProjectAction extends Actionable implements ProminentProjectAction {
    private static final String ICON_FILE = "/plugin/pitmutation/donatello.png";
    private static final String URL_NAME = "pitmutation";
    private static final String DISPLAY_NAME = "PIT Mutation Report";

    private final AbstractProject<?, ?> project;

    public PitProjectAction(AbstractProject<?, ?> project) {
        this.project = project;

    }

    //bylo w konstruktorze
    //    PitPublisher cp = (PitPublisher) project.getPublishersList().get(PitPublisher.DESCRIPTOR);
//    if (cp != null) {
//      onlyStable = cp.getOnlyStable();
//    }

    /**
     * JELLY
     * Getter for property 'lastResult'.
     *
     * @return Value for property 'lastResult'.
     */
    public PitBuildAction getLastResult() {
        for (AbstractBuild<?, ?> b = project.getLastSuccessfulBuild(); b != null; b = b.getPreviousNotFailedBuild()) {
            if (b.getResult() == Result.FAILURE) {
                continue;
            }
            PitBuildAction r = b.getAction(PitBuildAction.class);
            if (r != null) {
                return r;
            }
        }
        return null;
    }

    public AbstractProject<?, ?> getProject() {
        return project;
    }

    @Override
    public String getIconFileName() {
        return ICON_FILE;
    }

    @Override
    public String getUrlName() {
        return URL_NAME;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public String getSearchUrl() {
        return getUrlName();
    }

    public boolean isFloatingBoxActive() {
        return true;
    }

    //STAPLER
    public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException {
        Integer buildNumber = getLastResultBuild();
        if (buildNumber == null) {
            rsp.sendRedirect2("nodata");
        } else {
            rsp.sendRedirect2("../" + buildNumber + "/pitmutation");
        }
    }

    private Integer getLastResultBuild() {
        for (AbstractBuild<?, ?> b = project.getLastSuccessfulBuild(); b != null; b = b.getPreviousNotFailedBuild()) {
            if (b.getResult() == Result.FAILURE) {
                continue;
            }
            PitBuildAction r = b.getAction(PitBuildAction.class);
            if (r != null) {
                return b.getNumber();
            }
        }
        return null;
    }

    //STAPLER
    public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if (getLastResult() != null) {
            getLastResult().doGraph(req, rsp);
        }
    }
}
