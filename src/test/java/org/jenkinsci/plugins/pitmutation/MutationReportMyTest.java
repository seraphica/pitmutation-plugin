package org.jenkinsci.plugins.pitmutation;

import org.jenkinsci.plugins.pitmutation.targets.MutationStats;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author edward
 */
public class MutationReportMyTest {

    private static final String TESTED_CLASS = "com.awesomeness.dummy.details.AwesomenessGenerator";
    private final String MUTATIONS =
            "<mutations>" +
                    "<mutation detected='true' status='KILLED'>" +
                    "<sourceFile>AwesomenessGenerator.java</sourceFile>" +
                    "<mutatedClass>com.awesomeness.dummy.details.AwesomenessGenerator</mutatedClass>" +
                    "<mutatedMethod>appendBasicListing</mutatedMethod>" +
                    "<methodDescription>(Lcom/awesomeness/dummy/AnotherGreatIdea;Ljava/lang/StringBuilder;)V</methodDescription>" +
                    "<lineNumber>1239</lineNumber>" +
                    "<mutator>org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator</mutator>" +
                    "<index>43</index>" +
                    "<killingTest>com.awesomeness.dummy.details.AwesomenessGeneratorTest.testOutputOfGenerateDecisionDetailsForADD_BUZZ(com.awesomeness.dummy.details.AwesomenessGeneratorTest)</killingTest>" +
                    "</mutation>" +
                    "<mutation detected='false' status='NO_COVERAGE'>" +
                    "<sourceFile>AwesomenessGenerator.java</sourceFile>" +
                    "<mutatedClass>com.awesomeness.dummy.details.AwesomenessGenerator</mutatedClass>" +
                    "<mutatedMethod>appendHistorySummary</mutatedMethod>" +
                    "<methodDescription>(Lcom/awesomeness/dummy/AnotherGreatIdea;Ljava/lang/StringBuilder;)V</methodDescription>" +
                    "<lineNumber>288</lineNumber>" +
                    "<mutator>org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator</mutator>" +
                    "<index>7</index>" +
                    "<killingTest/>" +
                    "</mutation>" +
                    "</mutations>";

    private InputStream[] mutationsXml;

    @Before
    public void setUp() {
        mutationsXml = new InputStream[2];
        mutationsXml[0] = getClass().getResourceAsStream("mutations.xml");
    }

    @Test
    public void packageNameFinder() {
        assertThat(MutationReport.packageNameFromClass("xxx.yyy.zzz.Foo"), is("xxx.yyy.zzz"));
        assertThat(MutationReport.packageNameFromClass("Foo"), is(""));
    }

    @Test
    public void countsKills() throws IOException, SAXException {
        MutationReport report = MutationReport.create(mutationsXml[0]);

        MutationStats mutationStats = report.getMutationStats();

        assertThat(mutationStats.getKillCount(), is(12));
        assertThat(mutationStats.getTotalMutations(), is(54));
    }

    @Test
    public void sortsMutationsByClassName() throws IOException, SAXException {
        MutationReport report = MutationReport.create(mutationsXml[0]);

        Collection<Mutation> mutationsForClassName = report.getMutationsForClassName(TESTED_CLASS);

        assertThat(mutationsForClassName.size(), is(5));
    }

    @Test
    public void indexesMutationsByPackage() throws IOException, SAXException {
        MutationReport report = MutationReport.create(mutationsXml[0]);

        Collection<Mutation> mutationsForPackage = report.getMutationsForPackage("com.awesomeness.dummy");

        assertThat(mutationsForPackage, hasSize(49));

        assertThat(report.getMutationsForPackage(""), notNullValue());
        assertThat(report.getMutationsForPackage(""), hasSize(0));
    }

    @Test
    public void canDigestAMutation() throws IOException, SAXException {
        MutationReport report = MutationReport.create(new ByteArrayInputStream(MUTATIONS.getBytes()));

        assertThat(report.getMutationStats().getTotalMutations(), is(2));

        Iterator<Mutation> mutations =
                report.getMutationsForClassName(TESTED_CLASS).iterator();

        Mutation m1 = mutations.next();
        Mutation m2 = mutations.next();

        if (m1.getStatus().equals("KILLED")) { // bo nie zna kolejnosci chyba
            verifyMutationResultKilled(m1);
            verifyMutationResultNoCoverage(m2);
        } else {
            verifyMutationResultKilled(m2);
            verifyMutationResultNoCoverage(m1);
        }
    }

    private void verifyMutationResultNoCoverage(Mutation m) {
        assertThat(m.getLineNumber(), is(288));
        assertThat(m.isDetected(), is(false));
        assertThat(m.getStatus(), is("NO_COVERAGE"));
        assertThat(m.getSourceFile(), is("AwesomenessGenerator.java"));
        assertThat(m.getMutatedClass(), is(TESTED_CLASS));
        assertThat(m.getMutator(), is("org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator"));
    }

    private void verifyMutationResultKilled(Mutation m) {
        assertThat(m.getLineNumber(), is(1239));
        assertThat(m.isDetected(), is(true));
        assertThat(m.getStatus(), is("KILLED"));
        assertThat(m.getSourceFile(), is("AwesomenessGenerator.java"));
        assertThat(m.getMutatedClass(), is(TESTED_CLASS));
        assertThat(m.getMutator(), is("org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator"));
    }
}
