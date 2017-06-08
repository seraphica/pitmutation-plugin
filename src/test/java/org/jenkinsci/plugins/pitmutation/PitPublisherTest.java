package org.jenkinsci.plugins.pitmutation;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * User: Ed Kimber
 * Date: 17/03/13
 * Time: 17:55
 */
public class PitPublisherTest {

    private PitPublisher publisher;

    //  @Test
//  public void mutationReportPresenceCheck() {
//    publisher.mutationsReportExists();
//  }
    private float minimumKillRatio_ = 0.25f;

    @Before
    public void setup() {
        publisher = new PitPublisher("**/mutations.xml", minimumKillRatio_, true);

//    , mock(Launcher.class), mock(BuildListener.class)
    }


    @Test
    public void shouldGetModuleName() throws Exception {
        //given
//        String pathToTest = "/home/jenkins/work/jobs/polon-pit-mutation/workspace/common/target/pit-reports/201706071301/mutations.xml";
        String pathToTest = "D:\\tools\\Jenkins\\workspace\\mutant\\service-impl\\target\\pit-reports\\201706071301\\mutations.xml";

        //when
        String moduleName = publisher.extractModuleName(pathToTest);

        //then
        assertEquals("service-impl", moduleName);

    }

}
