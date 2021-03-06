package org.jenkinsci.plugins.pitmutation.targets;

import java.math.BigDecimal;

/**
 * @author Ed Kimber
 */
public abstract class MutationStats {

    private static final int ROUND_SCALE = 3;

    public abstract String getTitle();

    public abstract int getUndetected();

    public abstract int getTotalMutations();

    public int getKillCount() {
        return getTotalMutations() - getUndetected();
    }

    public float getKillPercent() {
        return round(100f * (float) (getTotalMutations() - getUndetected()) / (float) getTotalMutations());
    }

    private float round(float ratio) {
        if (Float.isNaN(ratio)) { // FIXME for developing purposes
            return 0;
        }
        if (Float.isInfinite(ratio)) {
            return ratio;
        }
        BigDecimal bd = new BigDecimal(ratio);
        BigDecimal rounded = bd.setScale(ROUND_SCALE, BigDecimal.ROUND_HALF_UP);
        return rounded.floatValue();
    }

    public MutationStats aggregate(final MutationStats other) {
        return new MutationStats() {
            @Override
            public String getTitle() {
                return MutationStats.this.getTitle() + ", " + other.getTitle();
            }

            @Override
            public int getUndetected() {
                return MutationStats.this.getUndetected() + other.getUndetected();
            }

            @Override
            public int getTotalMutations() {
                return MutationStats.this.getTotalMutations() + other.getTotalMutations();
            }
        };
    }

    public MutationStats delta(final MutationStats other) {
        return new MutationStats() {
            @Override
            public String getTitle() {
                return MutationStats.this.getTitle();
            }

            @Override
            public int getUndetected() {
                return MutationStats.this.getUndetected() - other.getUndetected();
            }

            @Override
            public int getTotalMutations() {
                return MutationStats.this.getTotalMutations() - other.getTotalMutations();
            }

            public float getKillPercent() {
                return round(MutationStats.this.getKillPercent() - other.getKillPercent());
            }
        };
    }
}
