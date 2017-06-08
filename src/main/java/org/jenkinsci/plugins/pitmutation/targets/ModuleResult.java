package org.jenkinsci.plugins.pitmutation.targets;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import org.jenkinsci.plugins.pitmutation.Mutation;
import org.jenkinsci.plugins.pitmutation.MutationReport;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author edward
 */
public class ModuleResult extends MutationResult<ModuleResult> implements Serializable {

    private static final Logger logger = Logger.getLogger(ModuleResult.class.getName());
    //    private static final Maps.EntryTransformer<String, Collection<Mutation>, MutationStats> statsTransformer_ =
//            new Maps.EntryTransformer<String, Collection<Mutation>, MutationStats>() {
//                public MutationStats transformEntry(String name, Collection<Mutation> mutations) {
//                    return new MutationStatsImpl(name, mutations);
//                }
//            };
//    private Map<String, MapDifference.ValueDifference<Collection<Mutation>>> mutationDifference_;
    private MutationReport report;
    private String name;

    public ModuleResult(String name, MutationResult parent, MutationReport report) {
        super(name, parent);
        this.name = name;
        this.report = report;
    }

    public String getDisplayName() {
        return getName();
    }

    @Override
    public MutationStats getMutationStats() {
        return report.getMutationStats();
    }

    public Map<String, MutatedPackage> getChildMap() {
        return Maps.transformEntries(report.getMutationsByPackage().asMap(),
                new Maps.EntryTransformer<String, Collection<Mutation>, MutatedPackage>() {
                    public MutatedPackage transformEntry(String name1, Collection<Mutation> mutations) {
                        logger.log(Level.FINER, "found " + report.getMutationsForPackage(name1).size() + " reports for " + name1);
                        return new MutatedPackage(name1, ModuleResult.this, Multimaps.index(report.getMutationsForPackage(name1), new Function<Mutation, String>() {
                            public String apply(Mutation mutation) {
                                return (mutation.getMutatedClass());
                            }
                        }));
                    }
                });
    }


    public String getName() {
        return name;
    }


//  public Collection<MutationStats> getStatsForNewTargets() {
//    return Maps.transformEntries(
//            Maps.difference(
//                    reports_.getFirst().getMutationsByClass().asMap(),
//                    reports_.getSecond().getMutationsByClass().asMap())
//                    .entriesOnlyOnLeft(),
//            statsTransformer_).values();
//  }

//  public Collection<Pair<MutatedClass>> getClassesWithNewSurvivors() {
//    return Maps.transformEntries(mutationDifference_, classMutationDifferenceTransform_).values();
//  }

//  private Maps.EntryTransformer<String, MapDifference.ValueDifference<Collection<Mutation>>, Pair<MutatedClass>> classMutationDifferenceTransform_ =
//          new Maps.EntryTransformer<String, MapDifference.ValueDifference<Collection<Mutation>>, Pair<MutatedClass>>() {
//            public Pair<MutatedClass> transformEntry(String name, MapDifference.ValueDifference<Collection<Mutation>> value) {
////              return MutatedClass.createPair(name, getOwner(), value.leftValue(), value.rightValue());
//            }
//          };

    public int compareTo(ModuleResult other) {
        return this.getMutationStats().getUndetected() - other.getMutationStats().getUndetected();
    }


}
