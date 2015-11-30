/*
 * Copyright (c) 2015, Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation and/or
 *     other materials provided with the distribution.
 *
 *  3. Neither the name of the copyright holder nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.sviperll.maven.plugin.versioning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class VersionSchema {
    private final Builder suffixMap;
    private VersionSchema(Builder suffixMap) {
        this.suffixMap = suffixMap;
    }
    
    boolean isPredecessorSuffix(String suffix) {
        Suffix suffixObject = suffixMap.getSuffix(suffix);
        return suffixObject.getOrderIndex() < 0;
    }

    boolean isFinalSuffix(String suffix) {
        Suffix suffixObject = suffixMap.getSuffix(suffix);
        return suffixObject.isFinalVersion();
    }

    int compareSuffixes(String suffix1, String suffix2) {
        Suffix suffixObject1 = suffixMap.getSuffix(suffix1);
        Suffix suffixObject2 = suffixMap.getSuffix(suffix2);
        int index1 = suffixObject1.getOrderIndex();
        int index2 = suffixObject2.getOrderIndex();
        int result = index1 < index2 ? -1 : (index1 == index2 ? 0 : 1);
        if (result != 0)
            return result;
        else {
            String canonical1 = getCanonicalSuffix(suffix1);
            String canonical2 = getCanonicalSuffix(suffix2);
            return canonical1.compareTo(canonical2);
        }
    }

    String getCanonicalSuffix(String suffix) {
        Suffix suffixObject = suffixMap.getSuffix(suffix);
        String canonicalString = suffixObject.getCanonicalString();
        return canonicalString == null ? suffix : canonicalString;
    }

    String getCanonicalFinalSuffix() {
        Suffix suffixObject = suffixMap.getFinalSuffix();
        return suffixObject.getCanonicalString();
    }

    VersionComponent finalVersionComponent() {
        return VersionComponent.finalVersionComponent(this);
    }

    VersionComponentScanner createScanner(String versionString) {
        return VersionComponentScanner.createInstance(this, versionString);
    }

    VersionComponentInstance finalVersionComponentInstance(String separator) {
        return new VersionComponentInstance(separator, finalVersionComponent());
    }

    VersionComponent numbersComponent(int[] numbers) {
        return VersionComponent.numbers(this, numbers);
    }

    VersionComponent suffixComponent(String suffix) {
        return VersionComponent.suffix(this, suffix);
    }

    VersionComponentInstance numbersComponentInstance(String separator, int[] numbers) {
        return new VersionComponentInstance(separator, numbersComponent(numbers));
    }

    VersionComponentInstance suffixComponentInstance(String separator, String suffix) {
        return new VersionComponentInstance(separator, suffixComponent(suffix));
    }

    Version version(String version) {
        return new Version(this, version);
    }

    Version versionOf(VersionComponentInstance... components) {
        return Version.of(this, components);
    }

    String[] getPredecessorSuffixes() {
        SortedMap<Integer, Suffix> map = new TreeMap<Integer, Suffix>();
        for (Suffix suffix: suffixMap.suffixes()) {
            if (suffix.getOrderIndex() < 0)
                map.put(suffix.getOrderIndex(), suffix);
        }
        String[] result = new String[map.size()];
        int i = 0;
        for (Map.Entry<Integer, Suffix> entry: map.entrySet()) {
            result[i] = entry.getValue().getCanonicalString();
            i++;
        }
        return result;
    }

    String getSuffixDescription(String suffix) {
        Suffix suffixObject = suffixMap.getSuffix(suffix);
        return suffixObject.getDescription();
    }

    String getNonEmptyFinalSuffix() {
        Suffix finalSuffix = suffixMap.getFinalSuffix();
        String canonicalSuffix = finalSuffix.getCanonicalString();
        if (!canonicalSuffix.equals(""))
            return canonicalSuffix;
        else {
            for (String suffix: finalSuffix.getVariants())
                if (!suffix.equals(""))
                    return suffix;
            throw new IllegalStateException("No non empty variant for final version suffixes");
        }
    }

    String[] getSuffixVariants(String suffix) {
        Suffix suffixObject = suffixMap.getSuffix(suffix);
        List<String> variants = suffixObject.getVariants();
        return variants.toArray(new String[variants.size()]);
    }
    static class Builder {
        private final Set<VersionSchema.Suffix> suffixes = new HashSet<VersionSchema.Suffix>();
        private final Map<String, VersionSchema.Suffix> suffixMap = new TreeMap<String, VersionSchema.Suffix>();
        private final VersionSchema.Suffix defaultSuffix = new Suffix(this);
        private final VersionSchema.Suffix finalSuffix = new Suffix(this);
        {
            finalSuffix.isFinalVersion = true;
            finalSuffix.addVariant("");
            finalSuffix.canonicalString = "";
        }

        private Suffix getSuffix(String suffixString) {
            Suffix result = suffixMap.get(suffixString);
            return result != null ? result : defaultSuffix;
        }
        
        Set<VersionSchema.Suffix> suffixes() {
            return suffixes;
        }

        VersionSchema.Suffix createSuffix() {
            VersionSchema.Suffix result = new VersionSchema.Suffix(this);
            suffixes.add(result);
            return result;
        }

        void setCanonicalSuffixString(String suffixString) {
            VersionSchema.Suffix suffix = suffixMap.get(suffixString);
            if (suffix == null) {
                suffix = new Suffix(this);
                suffix.addVariant(suffixString);
            }
            suffix.setCanonicalString(suffixString);
        }

        void setSuffixIndex(String suffixString, int i) {
            VersionSchema.Suffix suffix = suffixMap.get(suffixString);
            if (suffix == null) {
                suffix = new Suffix(this);
                suffix.addVariant(suffixString);
            }
            suffix.setOrderingIndex(i);
        }

        VersionSchema build() {
            return new VersionSchema(this);
        }

        private void putSuffix(String variant, Suffix suffix) {
            if (suffixMap.containsKey(variant))
                throw new IllegalStateException("Redefinition of suffix with " + variant + " variant");
            suffixMap.put(variant, suffix);
        }

        Suffix getFinalSuffix() {
            return finalSuffix;
        }
    }
    static class Suffix {
        String canonicalString = null;
        String description = null;
        private boolean isFinalVersion = false;
        private int index = 1;
        private final Builder builder;
        List<String> variants = new ArrayList<String>();

        private Suffix(Builder builder) {
            this.builder = builder;
        }

        void addVariant(String variant) {
            builder.putSuffix(variant, this);
            variants.add(variant);
        }

        void setDescription(String description) {
            this.description = description;
        }

        private void setCanonicalString(String suffixString) {
            canonicalString = suffixString;
        }

        private void setOrderingIndex(int index) {
            this.index = index;
        }

        private int getOrderIndex() {
            return isFinalVersion ? 0 : index;
        }

        private boolean isFinalVersion() {
            return isFinalVersion;
        }

        private String getCanonicalString() {
            return canonicalString != null ? canonicalString : (isFinalVersion ? "" : null);
        }

        private String getDescription() {
            return description;
        }

        private List<String> getVariants() {
            return variants;
        }
    }
}
