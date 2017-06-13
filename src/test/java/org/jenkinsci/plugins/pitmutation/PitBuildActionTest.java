package org.jenkinsci.plugins.pitmutation;

import hudson.model.AbstractBuild;
import hudson.model.Result;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintStream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author edward
 */
@Ignore
public class PitBuildActionTest {
    private PitBuildAction pitBuildAction;
    private AbstractBuild owner;
    private AbstractBuild failedBuild;
    private AbstractBuild successBuild;
    private MutationReport report;

    @Before
    public void setUp() {
        failedBuild = mock(AbstractBuild.class);
        when(failedBuild.getResult()).thenReturn(Result.FAILURE);
        successBuild = mock(AbstractBuild.class);
        when(successBuild.getResult()).thenReturn(Result.SUCCESS);
        owner = mock(AbstractBuild.class);
        File mockFileSys = mock(File.class);
        File mockFile = mock(File.class);
        File[] files = new File[1];
        files[0] = mockFile;
        when(mockFileSys.listFiles(any(FilenameFilter.class))).thenReturn(files);

        when(owner.getRootDir()).thenReturn(mockFileSys);
        report = mock(MutationReport.class);
        PrintStream printStream = mock(PrintStream.class);
        pitBuildAction = new PitBuildAction(owner, printStream);
    }

    @Test
    public void previousReturnsNullIfNoPreviousBuilds() {
        assertThat(pitBuildAction.getPreviousAction(), nullValue());
    }

    @Test
    public void previousReturnsNullIfAllPreviousBuildsFailed() {
        when(owner.getPreviousBuild()).thenReturn(failedBuild);
        assertThat(pitBuildAction.getPreviousAction(), nullValue());
    }

    @Test
    public void previousReturnsLastSuccessfulBuild() {
        PitBuildAction previousSucccessAction = mock(PitBuildAction.class);
        when(owner.getPreviousBuild()).thenReturn(failedBuild);
        when(failedBuild.getPreviousBuild()).thenReturn(successBuild);
        when(successBuild.getAction(PitBuildAction.class)).thenReturn(previousSucccessAction);

        assertThat(pitBuildAction.getPreviousAction(), is(previousSucccessAction));
    }


}
