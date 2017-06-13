package org.jenkinsci.plugins.pitmutation.targets;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.jenkinsci.plugins.pitmutation.Mutation;

import java.util.Collection;

/**
 * @author Ed Kimber
 */
public class MutationStatsImpl extends MutationStats {

    private String title;
    private int undetected = 0;
    private int total;
    private Multiset<String> mutationsByType;


    public MutationStatsImpl(String title, Collection<Mutation> mutations) {
        this.title = title;
        mutationsByType = HashMultiset.create();
        if (mutations != null) {
            for (Mutation m : mutations) {
                if (!m.isDetected()) {
                    undetected++;
                }
                mutationsByType.add(m.getMutatorClass());
            }
            total = mutations.size();
        }
    }

    public String getTitle() {
        return title;
    }

    public int getUndetected() {
        return undetected;
    }

    public int getTotalMutations() {
        return total;
    }
}

