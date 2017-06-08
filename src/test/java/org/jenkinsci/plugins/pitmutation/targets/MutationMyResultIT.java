package org.jenkinsci.plugins.pitmutation.targets;

import hudson.model.FreeStyleBuild;
import hudson.model.Run;
import org.jenkinsci.plugins.pitmutation.MutationReport;
import org.jenkinsci.plugins.pitmutation.PitBuildAction;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Ed Kimber
 */
public class MutationMyResultIT {

    private static final String TESTED_PACKAGE = "com.awesomeness.dummy";
    private static final String REPORT_OLD_FILE = "mutations_OLD.xml";
    private static final String REPORT_NEW_FILE = "mutations.xml";
    private static final String PACKAGE_SEPARATOR = ".";
    private static final String TESTED_CLASS = "ImportantServiceImpl";
    private static final String TESTED_CLASS_CANONIQUE = TESTED_PACKAGE + PACKAGE_SEPARATOR + TESTED_CLASS;
    private static final String MODULE_NAME = "module_name";
    private static final String SECOND_TESTING_PACKAGE = "com.awesomeness.dummy.details";

    private MutationResult<?> projectResult;
    private MutationResult<?> moduleResult;


    @Before
    public void setUp() throws IOException, SAXException {
        Map<String, MutationReport> reportsNew = new HashMap<String, MutationReport>();
        Map<String, MutationReport> reportsOld = new HashMap<String, MutationReport>();
        reportsNew.put(MODULE_NAME, MutationReport.create(getInputStream(REPORT_NEW_FILE)));
        reportsOld.put(MODULE_NAME, MutationReport.create(getInputStream(REPORT_OLD_FILE)));

        PitBuildAction pitBuildAction = mock(PitBuildAction.class); //M
        when(pitBuildAction.getReports()).thenReturn(reportsNew);
        Run build = mock(FreeStyleBuild.class);
        when(pitBuildAction.getOwner()).thenReturn(build);

        PitBuildAction previousBuildAction = mock(PitBuildAction.class); //M
        when(previousBuildAction.getReports()).thenReturn(reportsOld);
        when(previousBuildAction.getReport()).thenReturn(new ProjectMutations(previousBuildAction));
        when(pitBuildAction.getPreviousAction()).thenReturn(previousBuildAction);

        projectResult = new ProjectMutations(pitBuildAction);
        moduleResult = projectResult.getChildMap().get(MODULE_NAME);
    }

    private InputStream getInputStream(String reportOldFile) {
        return MutationReport.class.getResourceAsStream(reportOldFile);
    }

    @Test
    public void mutationResultStatsDelta() {
        MutationStats delta = projectResult.getStatsDelta();

        assertThat(delta.getTotalMutations(), is(22));
        assertThat(delta.getKillCount(), is(3));
    }

    @Test
    public void packageResultsStatsDelta() {
        MutationStats delta = projectResult.getChildResult(MODULE_NAME).getChildResult(TESTED_PACKAGE).getStatsDelta();

        assertThat(delta.getTotalMutations(), is(17));
        assertThat(delta.getKillCount(), is(1));
    }


    @Test
    public void classResultsStats() {
        MutationResult packageResult = projectResult.getChildResult(MODULE_NAME).getChildResult(TESTED_PACKAGE);
        MutationResult classResult = packageResult.getChildResult(TESTED_CLASS_CANONIQUE);

        MutationStats stats = classResult.getMutationStats();

        assertThat(stats.getTotalMutations(), is(49));
        assertThat(stats.getKillCount(), is(10));
    }

    @Test
    public void classResultsStatsDelta() {
        MutationResult packageResult = projectResult.getChildResult(MODULE_NAME).getChildResult(TESTED_PACKAGE);
        MutationResult classResult = packageResult.getChildResult(TESTED_CLASS_CANONIQUE);

        MutationStats delta = classResult.getStatsDelta();

        assertThat(delta.getTotalMutations(), is(17));
        assertThat(delta.getKillCount(), is(1));
    }

    @Test
    public void classResultsForNewClass() {
        MutationResult packageResult = projectResult.getChildResult(MODULE_NAME).getChildResult(SECOND_TESTING_PACKAGE);
        MutationResult classResult = packageResult.getChildResult(SECOND_TESTING_PACKAGE + PACKAGE_SEPARATOR + "AwesomenessGenerator");

        MutationStats stats = classResult.getMutationStats();

        assertThat(stats.getTotalMutations(), is(5));
        assertThat(stats.getKillCount(), is(2));
    }

    @Test
    public void classResultsForNewClassDelta() {
        MutationResult packageResult = projectResult.getChildResult(MODULE_NAME).getChildResult(SECOND_TESTING_PACKAGE);
        MutationResult classResult = packageResult.getChildResult(SECOND_TESTING_PACKAGE + PACKAGE_SEPARATOR + "AwesomenessGenerator");

        MutationStats stats = classResult.getStatsDelta();

        assertThat(stats.getTotalMutations(), is(5));
        assertThat(stats.getKillCount(), is(2));
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

    @Test
    public void findsClassesWithNewSurvivors() {
        Collection<? extends MutationResult> children = projectResult.getChildMap().get(MODULE_NAME)
                .getChildMap().get(TESTED_PACKAGE).getChildren();
        //        Map<String, ? extends MutationResult<?>> mutationResult = ((MutatedPackage) mutationResult1)
//                .getChildMap();
        Collection<MutatedClass> survivors = (Collection<MutatedClass>) children;
//        assertThat(survivors, hasSize(2));

        Iterator<MutatedClass> it = survivors.iterator();
        MutatedClass a = it.next();
//        MutatedClass b = it.next();
//        if (a.getFileName().equals(TESTED_CLASS + ".java.html")) {
        checkMutationJava(a);
        assertEquals("ImportantServiceImpl.java.html", a.getFileName());
        assertEquals("com.awesomeness.dummy.ImportantServiceImpl", a.getName());
        assertEquals("Class: com.awesomeness.dummy.ImportantServiceImpl", a.getDisplayName());
        assertEquals("com.awesomeness.dummy", a.getPackage());
        assertEquals("sdsddfdfsd", a.getSourceFileContent());
        assertEquals("sdsddfdfsd", a.getUrl());
//        assertEquals("sdsddfdfsd", a.get);
        assertFalse(a.isSourceLevel());

        //        checkPitBuildActionJava(a);
//        } else {
//            checkMutationJava(b);
//            checkPitBuildActionJava(a);
//        }
    }

    private void checkMutationJava(MutatedClass mutant) {
        assertThat(mutant.getFileName(), is(TESTED_CLASS + ".java.html"));
        assertThat(mutant.getChildMap().values(), hasSize(38));
    }

//    private void checkPitBuildActionJava(MutatedClass mutant) {
//        assertThat(mutant.getFileName(), is("PitBuildAction.java.html"));
//        assertThat(mutant.getChildMap().values(), hasSize(2));
//    }


    @Test
    public void urlTransformPackageName_My() {
        final String testedPackage = "com.awesomeness.dummy";

        String url = moduleResult.getChildMap().get(testedPackage).getUrl();

        assertThat(url,
                is("com_awesomeness_dummy"));
    }

    @Test
    public void urlTransformClassName_My() {
        final String testedPackage = "com.awesomeness.dummy";

        String url = moduleResult.getChildMap().get(testedPackage)
                .getChildMap().get(testedPackage + PACKAGE_SEPARATOR + TESTED_CLASS)
                .getUrl();

        assertThat(url,
                is("com_awesomeness_dummy_ImportantServiceImpl"));
    }

    @Test
    public void findsMutationsOnPitParserClass() {
        MutationResult<?> pitPackage = moduleResult.getChildMap().get(TESTED_PACKAGE);
        assertThat(pitPackage.getChildren(), hasSize(1));
        MutationResult<?> pitParser = pitPackage.getChildMap().get(TESTED_PACKAGE + PACKAGE_SEPARATOR + TESTED_CLASS);
        assertThat(pitParser.getChildren(), hasSize(38));
    }

    @Test
    public void collectsMutationStats() {
        MutationStats stats = projectResult.getMutationStats();
        assertThat(stats.getTotalMutations(), is(54));
        assertThat(stats.getUndetected(), is(42));
    }

    @Test
    public void correctSourceLevels() {
        MutationResult<?> pitPackage = moduleResult.getChildMap().get(TESTED_PACKAGE);
        MutationResult<?> pitParser = pitPackage.getChildMap().get(TESTED_PACKAGE + PACKAGE_SEPARATOR + TESTED_CLASS);
        MutationResult<?> lineResult = pitParser.getChildMap().values().iterator().next();

        assertThat(projectResult.isSourceLevel(), is(false));
        assertThat(moduleResult.isSourceLevel(), is(false));
        assertThat(pitPackage.isSourceLevel(), is(false));
        assertThat(pitParser.isSourceLevel(), is(true));
        assertThat(lineResult.isSourceLevel(), is(false));
    }

    @Test
    public void testXmlTransform() {
        assertThat(MutationResult.xmlTransform("replace&and<and>"), is("replace&amp;and&lt;and&gt;"));
    }

    @Test
    public void testUrlTransform() {
        assertThat(MutationResult.urlTransform("^*!replace::non+'alphas@}129"), is("___replace__non__alphas__129"));
    }

}
