package org.jenkinsci.plugins.pitmutation.targets;

import com.google.common.collect.*;
import org.jenkinsci.plugins.pitmutation.Mutation;

import java.util.Collection;

/**
 * @author edward
 */
public class MutatedLine {

  public static Collection<MutatedLine> createMutatedLines(Collection<Mutation> mutations) {
    Multimap<Integer, Mutation> multimap = TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());
    for (Mutation m : mutations) {
      multimap.put(m.getLineNumber(), m);
    }
    return Maps.transformEntries(multimap.asMap(), lineTransformer_).values();
  }

  public MutatedLine(int lineNumber, Collection<Mutation> mutations) {
    lineNumber_ = lineNumber;
    mutations_ = mutations;
  }

  public int getLineNumber() {
    return lineNumber_;
  }

  public Collection<Mutation> getMutations() {
    return mutations_;
  }

  public int getMutationCount() {
    return mutations_.size();
  }

  private int lineNumber_;
  private Collection<Mutation> mutations_;

  private static final Maps.EntryTransformer<Integer, Collection<Mutation>, MutatedLine> lineTransformer_ =
          new Maps.EntryTransformer<Integer, Collection<Mutation>, MutatedLine>() {
            public MutatedLine transformEntry(Integer line, Collection<Mutation> mutations) {
              return new MutatedLine(line, mutations);
            }
          };

}