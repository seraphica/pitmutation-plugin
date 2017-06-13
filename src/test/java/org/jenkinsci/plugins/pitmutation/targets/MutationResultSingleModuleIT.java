package org.jenkinsci.plugins.pitmutation.targets;

import hudson.model.FreeStyleBuild;
import hudson.model.Run;
import org.jenkinsci.plugins.pitmutation.MutationReport;
import org.jenkinsci.plugins.pitmutation.PitBuildAction;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Ed Kimber
 */
public class MutationResultSingleModuleIT {

    private static final String TESTED_PACKAGE = "com.awesomeness.dummy";
    private static final String REPORT_OLD_FILE = "mutations_OLD.xml";
    private static final String REPORT_NEW_FILE = "mutations.xml";
    private static final String PACKAGE_SEPARATOR = ".";
    private static final String TESTED_CLASS = "ImportantServiceImpl";
    private static final String TESTED_CLASS_CANONICAL = TESTED_PACKAGE + PACKAGE_SEPARATOR + TESTED_CLASS;
    private static final String MODULE_NAME = "awesome_module_name";
    private static final String SECOND_TESTING_PACKAGE = "com.awesomeness.dummy.details";
    private static final String ROOT_REPORT_FOLDER = "mutation-report";

    private ProjectMutations projectResult;
    private ModuleResult moduleResult;


    @Before
    public void setUp() throws IOException, SAXException {
        projectResult = new ProjectMutations(mockBuildAction());
//        moduleResult = (ModuleResult) projectResult.getChildMap().get(MODULE_NAME);
    }

//    @Test
//    public void mutationResultStatsDelta() {
//        MutationStats delta = projectResult.getStatsDelta();
//
//        assertThat(delta.getTotalMutations(), is(22));
//        assertThat(delta.getKillCount(), is(3));
//    }
//
//    @Test
//    public void packageResultsStatsDelta() {
//        MutationStats delta = projectResult.getChildResult(MODULE_NAME).getChildResult(TESTED_PACKAGE).getStatsDelta();
//
//        assertThat(delta.getTotalMutations(), is(17));
//        assertThat(delta.getKillCount(), is(1));
//    }
//
//
//    @Test
//    public void classResultsStats() {
//        MutationResult packageResult = projectResult.getChildResult(MODULE_NAME).getChildResult(TESTED_PACKAGE);
//        MutationResult classResult = packageResult.getChildResult(TESTED_CLASS_CANONICAL);
//
//        MutationStats stats = classResult.getMutationStats();
//
//        assertThat(stats.getTotalMutations(), is(49));
//        assertThat(stats.getKillCount(), is(10));
//    }
//
//    @Test
//    public void classResultsStatsDelta() {
//        MutationResult packageResult = projectResult.getChildResult(MODULE_NAME).getChildResult(TESTED_PACKAGE);
//        MutationResult classResult = packageResult.getChildResult(TESTED_CLASS_CANONICAL);
//
//        MutationStats delta = classResult.getStatsDelta();
//
//        assertThat(delta.getTotalMutations(), is(17));
//        assertThat(delta.getKillCount(), is(1));
//    }
//
//    @Test
//    public void classResultsForNewClass() {
//        MutationResult packageResult = projectResult.getChildResult(MODULE_NAME).getChildResult(SECOND_TESTING_PACKAGE);
//        MutationResult classResult = packageResult.getChildResult(SECOND_TESTING_PACKAGE + PACKAGE_SEPARATOR + "AwesomenessGenerator");
//
//        MutationStats stats = classResult.getMutationStats();
//
//        assertThat(stats.getTotalMutations(), is(5));
//        assertThat(stats.getKillCount(), is(2));
//    }
//
//    @Test
//    public void classResultsForNewClassDelta() {
//        MutationResult packageResult = projectResult.getChildResult(MODULE_NAME).getChildResult(SECOND_TESTING_PACKAGE);
//        MutationResult classResult = packageResult.getChildResult(SECOND_TESTING_PACKAGE + PACKAGE_SEPARATOR + "AwesomenessGenerator");
//
//        MutationStats stats = classResult.getStatsDelta();
//
//        assertThat(stats.getTotalMutations(), is(5));
//        assertThat(stats.getKillCount(), is(2));
//    }
//
//    @Test
//    public void classResultsOrdered() {
//        Iterator<? extends MutationResult> classes = moduleResult.getChildren().iterator();
//        int undetected = classes.next().getMutationStats().getUndetected();
//
//        while (classes.hasNext()) {
//            MutationResult result = classes.next();
//            assertThat(result.getMutationStats().getUndetected(), lessThan(undetected));
//            undetected = result.getMutationStats().getUndetected();
//        }
//    }
//
//    @Test
//    public void findsClassesWithNewSurvivors() {
//        Collection<? extends MutationResult> children = projectResult.getChildMap().get(TESTED_PACKAGE).getChildren();
//        //        Map<String, ? extends MutationResult<?>> mutationResult = ((MutatedPackage) mutationResult1)
////                .getChildMap();
//        Collection<MutatedClass> survivors = (Collection<MutatedClass>) children;
//        Iterator<MutatedClass> it = survivors.iterator();
//        MutatedClass mutatedClass = it.next();
////        MutatedClass b = it.next();
////        if (a.getFileName().equals(TESTED_CLASS + ".java.html")) {
//        assertThat(mutatedClass.getFileName(), is(TESTED_CLASS + ".java.html"));
//        assertThat(mutatedClass.getChildMap().values(), hasSize(38));
//        assertEquals("ImportantServiceImpl.java.html", mutatedClass.getFileName());
//        assertEquals("com.awesomeness.dummy.ImportantServiceImpl", mutatedClass.getName());
//        assertEquals("com.awesomeness.dummy.ImportantServiceImpl", mutatedClass.getDisplayName());
//        assertEquals("com.awesomeness.dummy", mutatedClass.getPackage());
//        assertEquals("com_awesomeness_dummy_ImportantServiceImpl", mutatedClass.getUrl());
//        assertThat(mutatedClass.isSourceLevel()).isTrue();
//    }
//

    @Test
    public void shouldReadSourceFileContentSingleModule() {
        Collection<? extends MutationResult> children = projectResult.getChildMap()
                .get(TESTED_PACKAGE).getChildren();
        //        Map<String, ? extends MutationResult<?>> mutationResult = ((MutatedPackage) mutationResult1)
//                .getChildMap();
        Iterator<MutatedClass> it = ((Collection<MutatedClass>) children).iterator();
        MutatedClass mutatedClass = it.next();
//        MutatedClass b = it.next();
//        if (a.getFileName().equals(TESTED_CLASS + ".java.html")) {
        assertThat(mutatedClass.getFileName(), is(TESTED_CLASS + ".java.html"));
        assertThat(mutatedClass.getChildMap().values(), hasSize(38));
        assertEquals("com_awesomeness_dummy_ImportantServiceImpl", mutatedClass.getUrl());

        assertThat(mutatedClass.getSourceFileContent()).isNotNull().startsWith("<html>");
        assertThat(mutatedClass.isSourceLevel()).isTrue();

    }

//
//    @Test
//    public void urlTransformPackageName_My() {
//        final String testedPackage = "com.awesomeness.dummy";
//
//        String url = moduleResult.getChildMap().get(testedPackage).getUrl();
//
//        assertThat(url,
//                is("com_awesomeness_dummy"));
//    }
//
//    @Test
//    public void urlTransformClassName_My() {
//        final String testedPackage = "com.awesomeness.dummy";
//
//        String url = moduleResult.getChildMap().get(testedPackage)
//                .getChildMap().get(testedPackage + PACKAGE_SEPARATOR + TESTED_CLASS)
//                .getUrl();
//
//        assertThat(url,
//                is("com_awesomeness_dummy_ImportantServiceImpl"));
//    }
//
//    @Test
//    public void findsMutationsOnPitParserClass() {
//        MutationResult<?> pitPackage = moduleResult.getChildMap().get(TESTED_PACKAGE);
//        assertThat(pitPackage.getChildren(), hasSize(1));
//        MutationResult<?> pitParser = pitPackage.getChildMap().get(TESTED_PACKAGE + PACKAGE_SEPARATOR + TESTED_CLASS);
//        assertThat(pitParser.getChildren(), hasSize(38));
//    }
//
//    @Test
//    public void collectsMutationStats() {
//        MutationStats stats = projectResult.getMutationStats();
//        assertThat(stats.getTotalMutations(), is(54));
//        assertThat(stats.getUndetected(), is(42));
//    }
//
//    @Test
//    public void correctSourceLevels() {
//        MutationResult<?> pitPackage = moduleResult.getChildMap().get(TESTED_PACKAGE);
//        MutationResult<?> pitParser = pitPackage.getChildMap().get(TESTED_PACKAGE + PACKAGE_SEPARATOR + TESTED_CLASS);
//        MutationResult<?> lineResult = pitParser.getChildMap().values().iterator().next();
//
//        assertThat(projectResult.isSourceLevel(), is(false));
//        assertThat(moduleResult.isSourceLevel(), is(false));
//        assertThat(pitPackage.isSourceLevel(), is(false));
//        assertThat(pitParser.isSourceLevel(), is(true));
//        assertThat(lineResult.isSourceLevel(), is(false));
//    }
//
//    @Test
//    public void testXmlTransform() {
//        assertThat(MutationResult.xmlTransform("replace&and<and>"), is("replace&amp;and&lt;and&gt;"));
//    }
//
//    @Test
//    public void testUrlTransform() {
//        assertThat(MutationResult.urlTransform("^*!replace::non+'alphas@}129"), is("___replace__non__alphas__129"));
//    }

    private PitBuildAction mockBuildAction() throws IOException, SAXException {
        PitBuildAction pitBuildAction = mock(PitBuildAction.class); //M
        when(pitBuildAction.getReports()).thenReturn(createNewMutationReportsMap());
        PitBuildAction previousBuild = mockPreviousBuild();
        when(pitBuildAction.getPreviousAction()).thenReturn(previousBuild);
        mockJenkinsBuild(pitBuildAction);
        return pitBuildAction;
    }

    private PitBuildAction mockPreviousBuild() throws IOException, SAXException {
        Map<String, MutationReport> reportsOld = createOldMutationReportsMap();
        PitBuildAction previousBuildAction = mock(PitBuildAction.class); //M
        when(previousBuildAction.getReports()).thenReturn(reportsOld);
//        when(previousBuildAction.getReport()).thenReturn(new ProjectMutations(previousBuildAction));
        return previousBuildAction;
    }

    private Map<String, MutationReport> createNewMutationReportsMap() throws IOException, SAXException {
        return createMutationReportsMap(MutationReport.create(getInputStream(REPORT_NEW_FILE)));
    }

    private Map<String, MutationReport> createOldMutationReportsMap() throws IOException, SAXException {
        return createMutationReportsMap(MutationReport.create(getInputStream(REPORT_OLD_FILE)));
    }

    private Map<String, MutationReport> createMutationReportsMap(MutationReport value) throws IOException, SAXException {
        Map<String, MutationReport> reportsOld = new HashMap<String, MutationReport>();
        reportsOld.put(MODULE_NAME, value);
        return reportsOld;
//        return value.getMutationsByPackage();
    }


    private void mockJenkinsBuild(PitBuildAction pitBuildAction) {
        Run build = mock(FreeStyleBuild.class); //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        when(pitBuildAction.getOwner()).thenReturn(build);
        when(build.getRootDir()).thenReturn(new File("myjob/13/"));
        //FIXME new File(project.getBuildDir(), Integer.toString(number)) - BUILD PATH/27/
    }

    private InputStream getInputStream(String reportOldFile) {
        return MutationReport.class.getResourceAsStream(reportOldFile);
    }


//    private void checkMutatedClass(MutatedClass mutant) {
//        assertThat(mutant.getFileName(), is(TESTED_CLASS + ".java.html"));
//        assertThat(mutant.getChildMap().values(), hasSize(38));
//    }

//    private void checkPitBuildActionJava(MutatedClass mutant) {
//        assertThat(mutant.getFileName(), is("PitBuildAction.java.html"));
//        assertThat(mutant.getChildMap().values(), hasSize(2));
//    }

}
