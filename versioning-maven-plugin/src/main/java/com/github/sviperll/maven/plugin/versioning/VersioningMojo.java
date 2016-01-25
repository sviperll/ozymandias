/*
 * Copyright 2015 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.maven.plugin.versioning;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
abstract class VersioningMojo extends AbstractMojo {
    /**
     * Version to base version selection dialog on.
     */
    @Parameter(property = "versioning.version", defaultValue = "${project.version}", required = true)
    String version;

    /*
     * <suffixes>
     *     <suffix>
     *         <variants>a,alpha,Alpha</variants>
     *     </suffix>
     *     <suffix>
     *         <variants>b,beta,Beta</variants>
     *     </suffix>
     *     <suffix>
     *         <description>release candidate</description>
     *         <variants>rc,RC,CR</variants>
     *     </suffix>
     * </suffixes>
     */
    @Parameter(required = false)
    List<Suffix> suffixes = new ArrayList<Suffix>();
    {
        Suffix suffix = new Suffix();
        suffix.variants = "a,alpha,Alpha";
        suffixes.add(suffix);
        suffix = new Suffix();
        suffix.variants = "b,beta,Beta";
        suffixes.add(suffix);
        suffix = new Suffix();
        suffix.variants = "rc,RC,CR";
        suffix.description = "release candidate";
        suffixes.add(suffix);
    }

    /*
     *     <finalVersionSuffix>
     *         <variants>final,Final,GA</variants>
     *     </finalVersionSuffix>
     */
    @Parameter(required = false)
    Suffix finalVersionSuffix = new Suffix();
    {
        finalVersionSuffix.variants = "final,Final,GA";
    }

    /*
     * <versionOrder>alpha,beta,rc</versionOrder>
     * or
     * <versionOrder>Alpha,Beta,CR,GA</versionOrder>
     */
    @Parameter(property = "versioning.order", required = false)
    String versionOrder = "alpha,beta,rc";

    @Component
    Prompter prompter;

    /**
    * The current Maven project or the super pom.
    */
    @Parameter(property = "project", readonly = true, required = true)
    MavenProject project;

    /**
    * The current Maven project or the super pom.
    */
    @Parameter(required = true, property = "session", readonly = true)
    MavenSession session;

    VersionSchema versionSchema;

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        init();
        executeInitialized();
    }

    protected void init() throws MojoExecutionException, MojoFailureException {
        VersionSchema.Builder builder = new VersionSchema.Builder();
        for (Suffix suffixConfiguration: suffixes) {
            String[] variants = suffixConfiguration.variants.split(",", -1);
            if (variants.length > 0) {
                VersionSchema.SuffixBuilder suffix = builder.getSuffixBuilder(variants[0].trim());
                for (String variant: variants) {
                    suffix.addVariant(variant.trim());
                }
                if (suffixConfiguration.description != null)
                    suffix.setDescription(suffixConfiguration.description);
            }
        }
        Suffix finalConfiguration = finalVersionSuffix;
        VersionSchema.SuffixBuilder finalSuffix = builder.getFinalSuffixBuilder();
        String[] variants = finalConfiguration.variants.split(",", -1);
        for (String variant: variants) {
            finalSuffix.addVariant(variant.trim());
        }
        if (finalConfiguration.description != null)
            finalSuffix.setDescription(finalConfiguration.description);
        String[] inOrderSuffixes = versionOrder.split(",", -1);
        for (int i = 0; i < inOrderSuffixes.length; i++) {
            String suffixString = inOrderSuffixes[i].trim();
            VersionSchema.SuffixBuilder suffixBuilder = builder.getSuffixBuilder(suffixString);
            if (!suffixBuilder.isFinalVersion()) {
                suffixBuilder.setCanonicalString(suffixString);
                suffixBuilder.setPredecessor(true);
                suffixBuilder.setOrderingIndex(i);
            } else {
                if (i != inOrderSuffixes.length - 1)
                    throw new MojoExecutionException("Final version suffix can only be used as a last element of version order list");
                else {
                    suffixBuilder.setCanonicalString(suffixString);
                    builder.setUseNonEmptyFinalSuffix(true);
                }
            }
        }

        // SNAPSHOT suffix is special and always present
        VersionSchema.SuffixBuilder suffixBuilder = builder.getSuffixBuilder("SNAPSHOT");
        suffixBuilder.setPredecessor(true);
        suffixBuilder.setOrderingIndex(inOrderSuffixes.length);

        versionSchema = builder.build();
    }

    abstract protected void executeInitialized() throws MojoExecutionException, MojoFailureException;

    String versionKind(String versionString) {
        VersionComponentScanner scanner = versionSchema.createScanner(versionString);
        scanner.getNextComponentInstance();
        VersionComponent selectedSuffix = scanner.getNextComponentInstance().component();
        if (selectedSuffix.isFinalComponent() && !scanner.hasMoreComponents())
            return "final";
        else if (selectedSuffix.isNumbers() || !selectedSuffix.isPredecessorSuffix())
            return "other";
        else
            return selectedSuffix.getCanonicalSuffixString();
    }

    String prompt(String message) throws MojoExecutionException {
        try {
            return prompter.prompt(message);
        } catch (PrompterException ex) {
            throw new MojoExecutionException("Unable to get version choice", ex);
        }
    }

    int menuPrompt(String title, ArrayList<String> menuIterms) throws MojoExecutionException {
        StringBuilder message = new StringBuilder();
        message.append(title).append("\n");
        for (int i = 0; i < title.length(); i++)
            message.append('-');
        message.append("\n\n");
        ArrayList<String> choices = new ArrayList<String>();
        int i;
        for (i = 0; i < menuIterms.size(); i++) {
            message.append(i + 1).append(". ").append(menuIterms.get(i)).append("\n");
            choices.add(Integer.toString(i + 1));
        }
        message.append("\n");
        String choiceString;
        try {
            choiceString = prompter.prompt(message.toString(), choices, Integer.toString(i));
        } catch (PrompterException ex) {
            throw new MojoExecutionException("Unable to get version choice", ex);
        }
        return choices.indexOf(choiceString);
    }

    void writeProperties(Properties properties, File file, Charset charset) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        try {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(bufferedOutputStream, charset);
                try {
                    BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
                    try {
                        properties.store(bufferedWriter, "Written by properties-maven-plugin");
                    } finally {
                        try {
                            bufferedWriter.close();
                        } catch (Exception ex) {
                            getLog().error("Error closing bufferedReader", ex);
                        }
                    }
                } finally {
                    try {
                        outputStreamWriter.close();
                    } catch (Exception ex) {
                        getLog().error("Error closing inputStreamReader", ex);
                    }
                }
            } finally {
                try {
                    bufferedOutputStream.close();
                } catch (Exception ex) {
                    getLog().error("Error closing bufferedInputStream", ex);
                }
            }
        } finally {
            try {
                fileOutputStream.close();
            } catch (Exception ex) {
                getLog().error("Error closing fileInputStream", ex);
            }
        }
    }

    Properties readProperties(File file, Charset charset) throws FileNotFoundException, IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(bufferedInputStream, charset);
                try {
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    try {
                        Properties properties = new Properties();
                        properties.load(bufferedReader);
                        return properties;
                    } finally {
                        try {
                            bufferedReader.close();
                        } catch (Exception ex) {
                            getLog().error("Error closing bufferedReader", ex);
                        }
                    }
                } finally {
                    try {
                        inputStreamReader.close();
                    } catch (Exception ex) {
                        getLog().error("Error closing inputStreamReader", ex);
                    }
                }
            } finally {
                try {
                    bufferedInputStream.close();
                } catch (Exception ex) {
                    getLog().error("Error closing bufferedInputStream", ex);
                }
            }
        } finally {
            try {
                fileInputStream.close();
            } catch (Exception ex) {
                getLog().error("Error closing fileInputStream", ex);
            }
        }
    }

}
