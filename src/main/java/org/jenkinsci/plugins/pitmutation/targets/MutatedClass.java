package org.jenkinsci.plugins.pitmutation.targets;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import hudson.util.TextFile;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.pitmutation.Mutation;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;


public class MutatedClass extends MutationResult<MutatedClass> {

    private static final String ROOT_REPORT_FOLDER = "mutation-report";

    private String name;
    private String packageName;
    private String fileName;
    private Collection<Mutation> mutations;
    private Map<String, MutatedLine> mutatedLines;

    public MutatedClass(String name, MutationResult parent, Collection<Mutation> mutations) {
        super(name, parent);
        this.name = name;
        this.mutations = mutations;
        this.packageName = getPackageName(name);
        this.fileName = getFileName(name);
        this.mutatedLines = createMutatedLines(mutations);
    }

    private String getFileName(String name) {
        int firstDollar = name.indexOf('$');
        int lastDot = name.lastIndexOf('.'); //FIXME paskudny zapis nizej
        return firstDollar >= 0
                ? (lastDot >= 0 ? name.substring(lastDot + 1, firstDollar) + ".java.html" : "")
                : (lastDot >= 0 ? name.substring(lastDot + 1) + ".java.html" : "");
    }

    private String getPackageName(String name) {
        int lastDotP = name.lastIndexOf('.');
        return lastDotP >= 0 ? name.substring(0, lastDotP) : "";
    }

    private Map<String, MutatedLine> createMutatedLines(Collection<Mutation> mutations) {
        HashMultimap<String, Mutation> multimap = HashMultimap.create();
        for (Mutation m : mutations) {
            multimap.put(String.valueOf(m.getLineNumber()), m);
        }
        return Maps.transformEntries(multimap.asMap(), new Maps.EntryTransformer<String, Collection<Mutation>, MutatedLine>() {
            public MutatedLine transformEntry(String line, Collection<Mutation> mutations1) {
                return new MutatedLine(line, MutatedClass.this, mutations1);
            }
        });
    }

    @Override
    public boolean isSourceLevel() {
        return true;
    }

    public String getSourceFileContent() {
        try {
//            return new TextFile(new File(getOwner().getRootDir(), "mutation-report/" + packageName + File.separator + fileName)).read();
            File file = new File(getOwner().getRootDir(), getClassReportPath());
            String absolutePath = file.getAbsolutePath(); //FIXME a gdzie module name?
            return new TextFile(file).read();

        } catch (IOException exception) {
            String exceptionMessage = "Could not read source file: " + getOwner().getRootDir().getPath() + File.separator
                    + getClassReportPath() + "\n";
            logger.log(Level.FINER, "NO TO KLOPS" + exceptionMessage); //FIXME
            return exceptionMessage;
        }
    }

    private String getClassReportPath() {
        return ROOT_REPORT_FOLDER + File.separator + getModuleName() + packageName + File.separator + fileName;
    }

    private String getModuleName() { //FIXME sprawdz dla jednomodulowego
        if (getParents().size() > 2) {
            return getParent().getParent().getName() + File.separator;
        }
        return StringUtils.EMPTY;
    }

    public String getDisplayName() {
        return getName();
    }

    @Override
    public MutationStats getMutationStats() {
        return new MutationStatsImpl(getName(), mutations);
    }

    public Map<String, ? extends MutationResult<?>> getChildMap() {
        return mutatedLines;
    }


    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPackage() {
        return packageName;
    }

    public int compareTo(MutatedClass other) {
        return this.getMutationStats().getUndetected() - other.getMutationStats().getUndetected();
    }

}
