package org.jenkinsci.plugins.pitmutation.targets;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.jenkinsci.plugins.pitmutation.Mutation;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Ed Kimber
 */
public class MutatedPackage extends MutationResult<MutatedPackage> {

    private Multimap<String, Mutation> classMutations;


    public MutatedPackage(String name, MutationResult parent, Multimap<String, Mutation> classMutations) {
        super(name, parent);
        this.classMutations = classMutations;
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public MutationStats getMutationStats() {
        return new MutationStatsImpl(getName(), classMutations.values());
    }

    @Override
    public Map<String, ? extends MutationResult<?>> getChildMap() {
        return Maps.transformEntries(classMutations.asMap(), new Maps.EntryTransformer<String, Collection<Mutation>, MutatedClass>() {
            public MutatedClass transformEntry(String name, Collection<Mutation> mutations) {
                logger.log(Level.FINER, "found " + mutations.size() + " reports for " + name);
                return new MutatedClass(name, MutatedPackage.this, mutations);
            }
        });
    }

    public int compareTo(MutatedPackage other) {
        return this.getMutationStats().getUndetected() - other.getMutationStats().getUndetected();
    }
}
