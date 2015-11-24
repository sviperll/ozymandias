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
    public void execute() throws MojoExecutionException, MojoFailureException {
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
        VersionComponentScanner scanner = VersionComponentScanner.createInstance(version);
        VersionComponentInstance numbers = scanner.getNextComponentInstance();
        VersionComponentInstance suffix = scanner.getNextComponentInstance();
        VersionComponentInstance suffixExtention = scanner.getNextComponentInstance();
        ArrayList<Version> versions = new ArrayList<Version>();
        Version base = new Version(version);
        Version candidate = Version.of(numbers, requiredSuffix.withTheSameSeparator(suffix));
        if (candidate.compareTo(base) >= 0) {
            versions.add(candidate);
            if (requiredSuffix.isExtensible())
                versions.add(candidate.extended(VersionComponentInstance.simpleExtention()));
        } else if (suffix.component().equals(requiredSuffix) && suffixExtention.isNumbers()) {
            candidate = Version.of(numbers, suffix, suffixExtention);
            if (candidate.compareTo(base) >= 0)
                versions.add(candidate);
            else {
                List<VersionComponent> suffixExtentionVariants = suffixExtention.component().nextNumbersComponentVariants(1, 3);
                for (VersionComponent variant: suffixExtentionVariants) {
                    candidate = Version.of(numbers, suffix, variant.withTheSameSeparator(suffixExtention));
                    if (candidate.compareTo(base) >= 0)
                        versions.add(candidate);
                }
            }
        } else if (numbers.isNumbers()) {
            List<VersionComponent> numbersVariants = numbers.component().deepNextNumbersComponentVariants(2, 3);
            for (VersionComponent variant: numbersVariants) {
                candidate = Version.of(variant.withTheSameSeparator(numbers), requiredSuffix.withTheSameSeparator(suffix));
                if (candidate.compareTo(base) >= 0) {
                    versions.add(candidate);
                    if (requiredSuffix.isExtensible()) {
                        versions.add(candidate.extended(VersionComponentInstance.simpleExtention()));
                    }
                }
            }
        }
        return versions;
    }

    private VersionComponent promptForKind() throws MojoExecutionException {
        String prompt = "Select version kind: alpha, beta, release candidate (rc), final release";
        String kind;
        try {
            kind = prompter.prompt(prompt, Arrays.asList("alpha", "beta", "rc", "final"), "final");
        } catch (PrompterException ex) {
            throw new MojoExecutionException("Unable to get version kind", ex);
        }
        if (kind.equals("alpha"))
            return VersionComponent.alpha();
        else if (kind.equals("beta"))
            return VersionComponent.beta();
        else if (kind.equals("rc"))
            return VersionComponent.rc();
        else if (kind.equals("final"))
            return VersionComponent.finalVersion();
        else
            throw new MojoExecutionException("Chosen value outside of allowed range");
    }
}
