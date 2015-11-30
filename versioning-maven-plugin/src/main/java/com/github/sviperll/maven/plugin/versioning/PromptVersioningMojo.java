/*
 * Copyright 2015 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.maven.plugin.versioning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.components.interactivity.PrompterException;

/**
 * Goal which read properties from file.
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@Mojo(name = "prompt", defaultPhase = LifecyclePhase.INITIALIZE, inheritByDefault = false, aggregator = true)
public class PromptVersioningMojo extends VersioningMojo {

    /**
     * Property name.
     * This property value will be set to selected version.
     */
    @Parameter(property = "versioning.version.decided.property", defaultValue = "versioning.version.decided", required = true)
    String decidedVersionPropertyName;

    @Override
    public void executeInitialized() throws MojoExecutionException, MojoFailureException {
        getLog().info("Version Selection");
        getLog().info("Current version is " + version);

        VersionComponent requiredSuffix = promptForKind();
        ArrayList<Version> versions = deviseCandidateVersions(requiredSuffix);

        int choice = promptForVersion(versions);
        String versionString;
        if (choice < versions.size()) {
            versionString = versions.get(choice).toString();
        } else {
            versionString = prompt("Enter custom version");
        }
        getLog().info("Selected version: " + versionString);
        project.getProperties().setProperty(decidedVersionPropertyName, versionString);
        project.getProperties().setProperty(decidedVersionPropertyName + ".kind", versionKind(versionString));
    }

    private int promptForVersion(ArrayList<Version> versions) throws MojoExecutionException {
        ArrayList<String> menuIterms = new ArrayList<String>();
        int i;
        for (i = 0; i < versions.size(); i++) {
            menuIterms.add(versions.get(i).toString());
        }
        menuIterms.add("<custom>");
        return menuPrompt("Choose variant", menuIterms);
    }

    private ArrayList<Version> deviseCandidateVersions(VersionComponent requiredSuffix) {
        VersionComponentScanner scanner = versionSchema.createScanner(version);
        VersionComponentInstance numbers = scanner.getNextComponentInstance();
        VersionComponentInstance suffix = scanner.getNextComponentInstance();
        VersionComponentInstance suffixExtention = scanner.getNextComponentInstance();
        ArrayList<Version> versions = new ArrayList<Version>();
        Version base = versionSchema.version(version);
        Version candidate = versionSchema.versionOf(numbers, requiredSuffix.withTheSameSeparator(suffix));
        if (candidate.compareTo(base) >= 0) {
            versions.add(candidate);
            if (requiredSuffix.allowsMoreComponents())
                versions.add(candidate.simpleExtention());
        } else if (suffix.component().equals(requiredSuffix) && suffixExtention.isNumbers()) {
            candidate = versionSchema.versionOf(numbers, suffix, suffixExtention);
            if (candidate.compareTo(base) >= 0)
                versions.add(candidate);
            else {
                List<VersionComponent> suffixExtentionVariants = suffixExtention.component().nextNumbersComponentVariants(1, 3);
                for (VersionComponent variant: suffixExtentionVariants) {
                    candidate = versionSchema.versionOf(numbers, suffix, variant.withTheSameSeparator(suffixExtention));
                    if (candidate.compareTo(base) >= 0)
                        versions.add(candidate);
                }
            }
        } else if (numbers.isNumbers()) {
            List<VersionComponent> numbersVariants = numbers.component().deepNextNumbersComponentVariants(2, 3);
            for (VersionComponent variant: numbersVariants) {
                candidate = versionSchema.versionOf(variant.withTheSameSeparator(numbers), requiredSuffix.withTheSameSeparator(suffix));
                if (candidate.compareTo(base) >= 0) {
                    versions.add(candidate);
                    if (requiredSuffix.allowsMoreComponents()) {
                        versions.add(candidate.simpleExtention());
                    }
                }
            }
        }
        return versions;
    }

    private VersionComponent promptForKind() throws MojoExecutionException {
        List<String> options = new ArrayList<String>();
        String[] predecessorSuffixes = versionSchema.getPredecessorSuffixes();
        StringBuilder prompt = new StringBuilder();
        prompt.append("Select version kind: ");
        for (String suffix: predecessorSuffixes) {
            prompt.append(suffixDescription(suffix)).append(", ");
            options.addAll(Arrays.asList(versionSchema.getSuffixVariants(suffix)));
        }

        String finalSuffix = versionSchema.getNonEmptyFinalSuffix();
        prompt.append(finalSuffix);
        options.addAll(Arrays.asList(versionSchema.getSuffixVariants(finalSuffix)));
        String kind;
        try {
            kind = prompter.prompt(prompt.toString(), options, finalSuffix);
        } catch (PrompterException ex) {
            throw new MojoExecutionException("Unable to get version kind", ex);
        }
        return versionSchema.suffixComponent(versionSchema.getCanonicalSuffix(kind));
    }

    private String suffixDescription(String suffix) {
        StringBuilder result = new StringBuilder();
        result.append(suffix);
        String description = versionSchema.getSuffixDescription(suffix);
        if (description != null) {
            result.append(" (").append(description).append(")");
        }
        return result.toString();
    }
}
