package org.jenkinsci.plugins.pitmutation;

import org.junit.Before;

/**
 * User: Ed Kimber
 * Date: 17/03/13
 * Time: 17:55
 */
public class PitPublisherTest {

    private PitPublisher publisher_;

//  @Test
//  public void mutationReportPresenceCheck() {
//    publisher_.mutationsReportExists();
//  }
    private float minimumKillRatio_ = 0.25f;

    @Before
    public void setup() {
        publisher_ = new PitPublisher("**/mutations.xml", minimumKillRatio_, true);

//    , mock(Launcher.class), mock(BuildListener.class)
    }

}
