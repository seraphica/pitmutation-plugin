package org.jenkinsci.plugins.pitmutation.targets;

import com.google.common.collect.Ordering;
import hudson.model.Run;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Ed Kimber
 */
public abstract class MutationResult<T extends MutationResult> implements Comparable<T> {

    static final Logger logger = Logger.getLogger(MutationResult.class.getName());

    private Run owner;
    private MutationResult parent;
    private String name;

    public MutationResult(String name, MutationResult parent) {
        this.parent = parent;
        this.name = name;
        this.owner = parent == null ? null : parent.getOwner();
    }

    public String getName() {
        return name;
    }

    public MutationResult getParent() {
        return parent;
    }

    public List<MutationResult> getParents() {
        List<MutationResult> result = new ArrayList<MutationResult>();
        MutationResult p = getParent();
        while (p != null) {
            result.add(p);
            p = p.getParent();
        }
        Collections.reverse(result);
        return result;
    }

    public MutationResult getPreviousResult() {
        if (parent == null) {
            return null;
        } else {
            MutationResult<?> previous = parent.getPreviousResult();
            return previous == null ? null : previous.getChildMap().get(name);
        }
    }

    public abstract String getDisplayName();

    public abstract MutationStats getMutationStats();

    public abstract Map<String, ? extends MutationResult<?>> getChildMap();

    public MutationResult<?> getChildResult(String name) {
        return getChildMap().get(name);
    }

    public boolean isSourceLevel() {
        return false;
    }

    public String getSourceFileContent() {
        return "";
    }

    public boolean isCoverageAltered() {
        return false;
    }

    public Collection<? extends MutationResult> getChildren() {
        return Ordering.natural().reverse().sortedCopy(getChildMap().values());
    }

    public MutationStats getStatsDelta() {
        MutationResult previous = getPreviousResult();
        return previous == null ? getMutationStats() : getMutationStats().delta(previous.getMutationStats());
    }

    public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) throws IOException {
        for (String name : getChildMap().keySet()) {
            if (urlTransform(name).equalsIgnoreCase(token)) {
                return getChildMap().get(name);
            }
        }
        return "#";
    }

    public Run<?, ?> getOwner() {
        return owner;
    }

    public String getUrl() {
        return urlTransform(getName());
    }

    public static String xmlTransform(String name) {
        return name.replaceAll("\\&", "&amp;").replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;");
    }

    public String relativeUrl(MutationResult parent) {
        StringBuilder url = new StringBuilder("..");

        MutationResult p = getParent();

        while (p != null && p != parent) {
            url.append("/..");
            p = p.getParent();
        }
        return url.toString();
    }

    static String urlTransform(String token) {
        StringBuilder buf = new StringBuilder(token.length());
        for (int i = 0; i < token.length(); i++) {
            final char c = token.charAt(i);
            if (('0' <= c && '9' >= c)
                    || ('A' <= c && 'Z' >= c)
                    || ('a' <= c && 'z' >= c)) {
                buf.append(c);
            } else {
                buf.append('_');
            }
        }
        return buf.toString();
    }

}
