package org.jenkinsci.plugins.pitmutation.targets;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PitReportResultsReaderTest {

    private PitReportResultsReader pitReportResultsReader;

    @Before
    public void setUp() throws Exception {
        pitReportResultsReader = new PitReportResultsReader();
    }

    @Test
    public void shouldReadReports() throws Exception {
        //given

        //when


        //then


    }

    @Test
    public void shouldExtractModuleName() throws Exception {
        //given

        //when


        //then


    }

    @Test
    public void shouldGetModuleName() throws Exception {
        //given
//      String pathToTest = "/home/jenkins/work/jobs/polon-pit-mutation/buils/28/mutation-report/service-impl/mutations.xml";
        String pathToTest = "D:\\tools\\Jenkins\\jobs\\mutant\\builds\\28\\mutation-report\\service-impl\\mutations.xml";

        //when
        String moduleName = pitReportResultsReader.extractModuleName(pathToTest);

        //then
        assertEquals("service-impl", moduleName);

    }

    @Test
    public void shouldGetModuleNameIfSingleModule() throws Exception {
        //given
//      String pathToTest = "/home/jenkins/work/jobs/polon-pit-mutation/buils/28/mutation-report/service-impl/mutations.xml";
        String pathToTest = "D:\\tools\\Jenkins\\jobs\\mutant\\builds\\28\\mutation-report\\mutations.xml";

        //when
        String moduleName = pitReportResultsReader.extractModuleName(pathToTest);

        //then
        assertEquals("", moduleName);

    }

}