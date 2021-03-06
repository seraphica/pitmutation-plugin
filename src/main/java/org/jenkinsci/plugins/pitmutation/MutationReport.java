package org.jenkinsci.plugins.pitmutation;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.apache.commons.digester3.Digester;
import org.jenkinsci.plugins.pitmutation.targets.MutationStats;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author edward
 */
public class MutationReport {

    private static final Logger logger = Logger.getLogger(MutationReport.class.getName());
    private static final Set<Mutation> EMPTY_SET = new HashSet<Mutation>();
    public static Predicate<Mutation> isSurvivor = new Predicate<Mutation>() {
        public boolean apply(Mutation mutation) {
            return !mutation.isDetected();
        }
    };
    private Multimap<String, Mutation> mutationsByPackage;
    private Multimap<String, Mutation> mutationsByClass;
    private int killCount_ = 0;

    public MutationReport() {
        mutationsByClass = HashMultimap.create();
    }

    public static MutationReport create(InputStream xmlReport) throws IOException, SAXException {
        return digestMutations(xmlReport);
    }

    private static MutationReport digestMutations(InputStream input) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.addObjectCreate("mutations", MutationReport.class);
        digester.addObjectCreate("mutations/mutation", Mutation.class);
        digester.addSetNext("mutations/mutation", "addMutation", "org.jenkinsci.plugins.pitmutation.Mutation");
        digester.addSetProperties("mutations/mutation");
        digester.addSetNestedProperties("mutations/mutation");

        MutationReport report = digester.parse(input);
        report.mutationsByPackage = Multimaps.index(report.mutationsByClass.values(), new Function<Mutation, String>() {
            public String apply(Mutation mutation) {
                return packageNameFromClass(mutation.getMutatedClass());
            }
        });
        return report;
    }

    /**
     * Called by digester.
     *
     * @param mutation
     */
    public void addMutation(Mutation mutation) {
        mutationsByClass.put(mutation.getMutatedClass(), mutation);
        if (mutation.isDetected()) {
            killCount_++;
        }
        mutationsByPackage = Multimaps.index(mutationsByClass.values(), new Function<Mutation, String>() {
            public String apply(Mutation mutation1) {
                return packageNameFromClass(mutation1.getMutatedClass());
            }
        });
    }

    public Collection<Mutation> getMutationsForPackage(String packageName) {
        return mutationsByPackage.get(packageName);
    }

    public Multimap<String, Mutation> getMutationsByPackage() {
        return mutationsByPackage;
    }

    public Collection<Mutation> getMutationsForClassName(String className) {
        Collection<Mutation> mutations = mutationsByClass.get(className);
        return mutations != null ? mutations : EMPTY_SET;
    }

    public MutationStats getMutationStats() {
        return new MutationStats() {
            public String getTitle() {
                return "Report Stats";
            }

            public int getUndetected() {
                return getTotalMutations() - killCount_;
            }

            public int getTotalMutations() {
                return mutationsByClass.values().size();
            }
        };
    }

    static String packageNameFromClass(String fqcn) {
        int idx = fqcn.lastIndexOf('.');
        return fqcn.substring(0, idx != -1 ? idx : 0);
    }

}
