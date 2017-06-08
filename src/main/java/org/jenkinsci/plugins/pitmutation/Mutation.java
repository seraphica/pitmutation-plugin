package org.jenkinsci.plugins.pitmutation;

/**
 * @author edward
 */
public class Mutation {

    enum Status {
        KILLED,
        NO_COVERAGE
    }

    private boolean detected;
    private String status;
    private String file;
    private String clsName;
    private String methodName;
    private int lineNumber;
    private String mutator;
    private int index;
    private String killedTest;
    private String methodDescription;



    public boolean isDetected() {
        return detected;
    }

    //TODO better equals
    public boolean equals(Mutation m) {
        return m.getMutatedClass().equals(getMutatedClass())
                && m.getMutatedMethod().equals(getMutatedMethod())
                && m.getLineNumber() == getLineNumber()
                && m.getMutator().equals(getMutator())
                && m.getStatus().equals(getStatus())
                && m.getIndex() == getIndex();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Mutation && this.equals((Mutation) o);
    }

    @Override
    public int hashCode() {
        return getMutatedClass() == null ? 1 : getMutatedClass().hashCode() ^ getMutatedClass().hashCode();
    }

    public void setDetected(final boolean detected) {
        this.detected = detected;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getSourceFile() {
        return file;
    }

    public void setSourceFile(final String file) {
        this.file = file;
    }

    public String getMutatedClass() {
        return clsName;
    }

    public void setMutatedClass(final String clsName) {
        this.clsName = clsName;
    }

    public String getMutatedMethod() {
        return methodName;
    }

    public void setMutatedMethod(final String method) {
        methodName = method;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(final int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getMutator() {
        return mutator;
    }

    public void setMutator(final String mutator) {
        this.mutator = mutator;
    }

    public String getMutatorClass() {
        int lastDot = mutator.lastIndexOf('.');
        String className = mutator.substring(lastDot + 1);
        return className.endsWith("Mutator")
                ? className.substring(0, className.length() - 7)
                : className;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public String getKillingTest() {
        return killedTest;
    }

    public void setKillingTest(final String killedTest) {
        this.killedTest = killedTest;
    }

    public String getMethodDescription() {
        return methodDescription;
    }

    public void setMethodDescription(final String methodDescription) {
        this.methodDescription = methodDescription;
    }

    public String toString() {
        return file + ":" + lineNumber + " : " + status + " type:[" + mutator + "]";
    }


    public static final class MutationBuilder {
        private String clsName;
        private boolean detected;
        private String status;
        private String file;
        private String methodName;
        private int lineNumber;
        private String mutator;
        private int index;
        private String killedTest;
        private String methodDescription;

        private MutationBuilder() {
        }

        public static MutationBuilder aMutation() {
            return new MutationBuilder();
        }

        public MutationBuilder withClsName(String clsName) {
            this.clsName = clsName;
            return this;
        }

        public MutationBuilder withDetected(boolean detected) {
            this.detected = detected;
            return this;
        }

        public MutationBuilder withStatus(String status) {
            this.status = status;
            return this;
        }

        public MutationBuilder withFile(String file) {
            this.file = file;
            return this;
        }

        public MutationBuilder withMethodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public MutationBuilder withLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
            return this;
        }

        public MutationBuilder withMutator(String mutator) {
            this.mutator = mutator;
            return this;
        }

        public MutationBuilder withIndex(int index) {
            this.index = index;
            return this;
        }

        public MutationBuilder withKilledTest(String killedTest) {
            this.killedTest = killedTest;
            return this;
        }

        public MutationBuilder withMethodDescription(String methodDescription) {
            this.methodDescription = methodDescription;
            return this;
        }

        public Mutation build() {
            Mutation mutation = new Mutation();
            mutation.setDetected(detected);
            mutation.setStatus(status);
            mutation.setLineNumber(lineNumber);
            mutation.setMutator(mutator);
            mutation.setIndex(index);
            mutation.setMethodDescription(methodDescription);
            mutation.methodName = this.methodName;
            mutation.file = this.file;
            mutation.clsName = this.clsName;
            mutation.killedTest = this.killedTest;
            return mutation;
        }
    }
}
