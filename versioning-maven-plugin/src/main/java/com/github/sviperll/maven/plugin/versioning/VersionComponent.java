/*
 * Copyright 2015 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.maven.plugin.versioning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
abstract class VersionComponent implements Comparable<VersionComponent> {

    static VersionComponent numbers(VersionSchema schema, int[] numbers) {
        return new NumbersVersionComponent(schema, numbers);
    }

    static VersionComponent suffix(VersionSchema schema, String suffix) {
        boolean isPredecessorSuffix = schema.isPredecessorSuffix(suffix);
        boolean isFinalComponent = schema.isFinalSuffix(suffix);
        return new SuffixVersionComponent(schema, suffix, isPredecessorSuffix, isFinalComponent);
    }

    static VersionComponent finalVersionComponent(VersionSchema schema) {
        return suffix(schema, schema.getCanonicalFinalSuffix());
    }

    private final VersionSchema schema;

    private VersionComponent(VersionSchema schema) {
        this.schema = schema;
    }

    abstract boolean isNumbers();
    abstract boolean isSuffix();
    abstract boolean isPredecessorSuffix();
    abstract boolean isFinalComponent();
    abstract int[] getNumbers();
    abstract String getSuffixString();

    VersionSchema schema() {
        return schema;
    }

    boolean allowsMoreComponents() {
        return !isFinalComponent();
    }

    VersionComponentInstance withTheSameSeparator(VersionComponentInstance component) {
        return component.withTheSameSeparator(this);
    }

    List<VersionComponent> nextNumbersComponentVariants(int minDepth, int maxDepth) {
        return nextNumbersComponentVariants(minDepth, maxDepth, false);
    }
    private List<VersionComponent> nextNumbersComponentVariants(int minDepth, int maxDepth, boolean goesDeeper) {
        List<VersionComponent> result = new ArrayList<VersionComponent>();
        int[] numbers = getNumbers();
        int reachableDepth = goesDeeper ? maxDepth : Math.min(numbers.length, maxDepth);
        for (int depth = minDepth; depth <= reachableDepth; depth++) {
            for (int i = 0; i < depth; i++) {
                int[] variant = Arrays.copyOf(numbers, depth);
                variant[i] = i < numbers.length ? numbers[i] + 1 : 1;
                for (int j = i + 1; j < depth; j++)
                    variant[j] = 0;
                result.add(VersionComponent.numbers(schema, variant));
            }
        }
        return result;
    }

    List<VersionComponent> deepNextNumbersComponentVariants(int minDepth, int maxDepth) {
        return nextNumbersComponentVariants(minDepth, maxDepth, true);
    }

    String defaultSeparator() {
        return isSuffix() && getCanonicalSuffixString().isEmpty() ? "" : "-";
    }

    String getCanonicalSuffixString() {
        return schema.getCanonicalSuffix(getSuffixString());
    }

    boolean isLessThanOrEqualsToFinal() {
        return compareTo(schema.finalVersionComponent()) <= 0;
    }

    @Override
    public final int compareTo(final VersionComponent that) {
        if (this.schema != that.schema)
            throw new IllegalStateException("Comparison of version component with different suffix schema");
        if (this.isPredecessorSuffix()) {
            if (!that.isPredecessorSuffix())
                return -1;
            else {
                return this.schema.compareSuffixes(this.getSuffixString(), that.getSuffixString());
            }
        } else if (this.isFinalComponent()) {
            if (that.isPredecessorSuffix())
                return 1;
            else if (that.isFinalComponent())
                return 0;
            else
                return -1;
        } else if (this.isNumbers()) {
            if (that.isPredecessorSuffix() || that.isFinalComponent())
                return 1;
            else if (that.isNumbers()) {
                int[] thisNumbers = this.getNumbers();
                int[] thatNumbers = that.getNumbers();
                for (int i = 0; ; i++) {
                    if (i >= thisNumbers.length && i >= thatNumbers.length)
                        break;
                    else if (i >= thisNumbers.length)
                        return -1;
                    else if (i >= thatNumbers.length)
                        return 1;
                    else {
                        int thisElement = thisNumbers[i];
                        int thatElement = thatNumbers[i];
                        int result = thisElement < thatElement ? -1 : (thisElement == thatElement ? 0 : 1);
                        if (result != 0)
                            return result;
                    }
                }
                return 0;
            } else
                return -1;
            
        } else {
            if (that.isPredecessorSuffix() || that.isFinalComponent() || that.isNumbers())
                return 1;
            else
                return schema.compareSuffixes(this.getSuffixString(), that.getSuffixString());
        }
    }

    @Override
    public final int hashCode() {
        if (isNumbers()) {
            int[] numbers = getNumbers();
            int result = 6;
            for (int i = 0; i < numbers.length; i++) {
                result = result * 37 + numbers[i];
            }
            return result;
        } else {
            if (isPredecessorSuffix())
                return (1 * 37 + schema.hashCode()) * 37 + getCanonicalSuffixString().hashCode();
            else if (isFinalComponent())
                return 2 * 37 + schema.hashCode();
            else
                return (3 * 37 + schema.hashCode()) * 37 + getCanonicalSuffixString().hashCode();
        }
    }

    @Override
    public final String toString() {
        if (isNumbers()) {
            StringBuilder builder = new StringBuilder();
            int[] numbers = getNumbers();
            if (numbers.length > 0) {
                builder.append(numbers[0]);
                for (int i = 1; i < numbers.length; i++) {
                    builder.append('.');
                    builder.append(numbers[i]);
                }
            }
            return builder.toString();
        } else {
            return getSuffixString();
        }
    }

    @Override
    public final boolean equals(Object thatObject) {
        if (this == thatObject)
            return true;
        else if (!(thatObject instanceof VersionComponent))
            return false;
        else {
            VersionComponent that = (VersionComponent)thatObject;
            return this.compareTo(that) == 0;
        }
    }

    private static class SuffixVersionComponent extends VersionComponent {

        private final boolean isPredecessorSuffix;
        private final boolean isFinalComponent;
        private final String suffix;

        public SuffixVersionComponent(VersionSchema schema, String suffix, boolean isPredecessorSuffix, boolean isFinalComponent) {
            super(schema);
            this.isPredecessorSuffix = isPredecessorSuffix;
            this.isFinalComponent = isFinalComponent;
            this.suffix = suffix;
        }

        @Override
        boolean isNumbers() {
            return false;
        }

        @Override
        boolean isSuffix() {
            return true;
        }

        @Override
        boolean isPredecessorSuffix() {
            return isPredecessorSuffix;
        }

        @Override
        boolean isFinalComponent() {
            return isFinalComponent;
        }
                            
        @Override
        int[] getNumbers() {
            throw new UnsupportedOperationException();
        }

        @Override
        String getSuffixString() {
            return suffix;
        }
    }

    private static class NumbersVersionComponent extends VersionComponent {

        private final int[] numbers;

        public NumbersVersionComponent(VersionSchema schema, int[] numbers) {
            super(schema);
            this.numbers = numbers;
        }

        @Override
        boolean isNumbers() {
            return true;
        }

        @Override
        boolean isSuffix() {
            return false;
        }

        @Override
        boolean isPredecessorSuffix() {
            return false;
        }

        @Override
        boolean isFinalComponent() {
            return false;
        }

        @Override
        int[] getNumbers() {
            return numbers;
        }

        @Override
        String getSuffixString() {
            throw new UnsupportedOperationException();
        }
    }
}
