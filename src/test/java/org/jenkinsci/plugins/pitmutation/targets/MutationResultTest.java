package org.jenkinsci.plugins.pitmutation.targets;

import org.jenkinsci.plugins.pitmutation.MutationReport;
import org.jenkinsci.plugins.pitmutation.PitBuildAction;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Ed Kimber
 */
public class MutationResultTest {
    private MutationResult<?> mutationResult;
    private MutationResult<?> moduleResult;

    @Before
    public void setUp() throws IOException, SAXException {
        InputStream[] mutationsXml_ = new InputStream[2];
        mutationsXml_[0] = MutationReport.class.getResourceAsStream("testmutations-00.xml");
        mutationsXml_[1] = MutationReport.class.getResourceAsStream("testmutations-01.xml");
        MutationReport reportOld = MutationReport.create(mutationsXml_[0]);
        MutationReport reportNew = MutationReport.create(mutationsXml_[1]);
        Map<String, MutationReport> reportsNew = new HashMap<String, MutationReport>();
        Map<String, MutationReport> reportsOld = new HashMap<String, MutationReport>();
        reportsNew.put("test_report", reportNew);
        reportsOld.put("test_report", reportOld);


        PitBuildAction buildAction_ = mock(PitBuildAction.class);
        when(buildAction_.getReports()).thenReturn(reportsNew);

        PitBuildAction previousBuildAction_ = mock(PitBuildAction.class);
        when(previousBuildAction_.getReports()).thenReturn(reportsOld);
        when(previousBuildAction_.getReport()).thenReturn(new ProjectMutations(previousBuildAction_));

        when(buildAction_.getPreviousAction()).thenReturn(previousBuildAction_);


        mutationResult = new ProjectMutations(buildAction_);
        moduleResult = mutationResult.getChildMap().get("test_report");
        assertThat(moduleResult, not(nullValue()));
        assertThat(mutationResult.getPreviousResult(), not(nullValue()));
    }

    @Test
    public void mutationResultStatsDelta() {
        MutationStats delta = mutationResult.getStatsDelta();
        assertThat(delta.getTotalMutations(), is(3));
        assertThat(delta.getKillCount(), is(-1));
    }

    private MutationResult packageResult() {
        return mutationResult.getChildResult("test_report").getChildResult("org.jenkinsci.plugins.pitmutation");
    }

    @Test
    public void packageResultsStatsDelta() {
        MutationStats delta = packageResult().getStatsDelta();
        assertThat(delta.getTotalMutations(), is(3));
        assertThat(delta.getKillCount(), is(-1));
    }

    private MutationResult classResult(String className) {
        return packageResult().getChildResult(className);
    }

    @Test
    public void classResultsStats() {
        MutationStats stats = classResult("org.jenkinsci.plugins.pitmutation.Mutation").getMutationStats();
        assertThat(stats.getTotalMutations(), is(3));
        assertThat(stats.getKillCount(), is(1));
    }

    @Test
    public void classResultsStatsDelta() {
        MutationStats delta = classResult("org.jenkinsci.plugins.pitmutation.Mutation").getStatsDelta();
        assertThat(delta.getTotalMutations(), is(-1));
        assertThat(delta.getKillCount(), is(-2));
    }

    @Test
    public void classResultsForNewClass() {
        MutationStats stats = classResult("org.jenkinsci.plugins.pitmutation.NewMutatedClass").getMutationStats();
        assertThat(stats.getTotalMutations(), is(1));
        assertThat(stats.getKillCount(), is(0));
    }

    @Test
    public void classResultsForNewClassDelta() {
        MutationStats stats = classResult("org.jenkinsci.plugins.pitmutation.NewMutatedClass").getStatsDelta();
        assertThat(stats.getTotalMutations(), is(1));
        assertThat(stats.getKillCount(), is(0));
    }

    @Test
    public void classResultsOrdered() {
        Iterator<? extends MutationResult> classes = moduleResult.getChildren().iterator();
        int undetected = classes.next().getMutationStats().getUndetected();

        while (classes.hasNext()) {
            MutationResult result = classes.next();
            assertThat(result.getMutationStats().getUndetected(), lessThan(undetected));
            undetected = result.getMutationStats().getUndetected();
        }
    }
//  @Test
//  public void findsClassesWithNewSurvivors() {
//    Collection<MutatedClass> survivors = result_.getClassesWithNewSurvivors();
//    assertThat(survivors, hasSize(2));
//
//    Iterator<MutatedClass> it = survivors.iterator();
//    MutatedClass a = it.next();
//    MutatedClass b = it.next();
//    if (a.getFileName().equals("Mutation.java.html")) {
//      checkMutationJava(a);
//      checkPitBuildActionJava(b);
//    }
//    else {
//      checkMutationJava(b);
//      checkPitBuildActionJava(a);
//    }
//  }
//
//  private void checkMutationJava(MutatedClass mutant) {
//    assertThat(mutant.getFileName(), is("Mutation.java.html"));
//    assertThat(mutant.getMutatedLines(), hasSize(1));
//  }
//
//  private void checkPitBuildActionJava(MutatedClass mutant) {
//    assertThat(mutant.getFileName(), is("PitBuildAction.java.html"));
//    assertThat(mutant.getMutatedLines(), hasSize(2));
//  }

    @Test
    public void urlTransformPackageName() {
        assertThat(moduleResult.getChildMap().get("org.jenkinsci.plugins.pitmutation").getUrl(),
                is("org_jenkinsci_plugins_pitmutation"));
    }

    @Test
    public void urlTransformClassName() {
        assertThat(moduleResult.getChildMap().get("org.jenkinsci.plugins.pitmutation")
                        .getChildMap().get("org.jenkinsci.plugins.pitmutation.PitParser").getUrl(),
                is("org_jenkinsci_plugins_pitmutation_PitParser"));
    }

    @Test
    public void findsMutationsOnPitParserClass() {
        MutationResult<?> pitPackage = moduleResult.getChildMap().get("org.jenkinsci.plugins.pitmutation");
        assertThat(pitPackage.getChildren(), hasSize(5));
        MutationResult<?> pitParser = pitPackage.getChildMap().get("org.jenkinsci.plugins.pitmutation.PitParser");
        assertThat(pitParser.getChildren(), hasSize(3));
    }

    @Test
    public void collectsMutationStats() {
        MutationStats stats = mutationResult.getMutationStats();
        assertThat(stats.getTotalMutations(), is(19));
        assertThat(stats.getUndetected(), is(15));
    }

    @Test
    public void correctSourceLevels() {
        MutationResult<?> pitPackage = moduleResult.getChildMap().get("org.jenkinsci.plugins.pitmutation");
        MutationResult<?> pitParser = pitPackage.getChildMap().get("org.jenkinsci.plugins.pitmutation.PitParser");
        MutationResult<?> lineResult = pitParser.getChildMap().values().iterator().next();

        assertThat(mutationResult.isSourceLevel(), is(false));
        assertThat(moduleResult.isSourceLevel(), is(false));
        assertThat(pitPackage.isSourceLevel(), is(false));
        assertThat(pitParser.isSourceLevel(), is(true));
        assertThat(lineResult.isSourceLevel(), is(false));
    }
//
//  @Test
//  public void collectsStatsOnNewTargets() {
//    Collection<MutationStats> newTargetStats = result_.getStatsForNewTargets();
//    assertThat(newTargetStats, hasSize(1));
//
//    MutationStats stats = newTargetStats.iterator().next();
//    assertThat(stats.getTotalMutations(), is(1));
//    assertThat(stats.getUndetected(), is(1));
//  }

    @Test
    public void testXmlTransform() {
        assertThat(MutationResult.xmlTransform("replace&and<and>"), is("replace&amp;and&lt;and&gt;"));
    }

    @Test
    public void testUrlTransform() {
        assertThat(MutationResult.urlTransform("^*!replace::non+'alphas@}129"), is("___replace__non__alphas__129"));
    }
}
