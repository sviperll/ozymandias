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
import java.util.Properties;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
abstract class VersioningMojo extends AbstractMojo {
    /**
     * Version to base version selection dialog on.
     */
    @Parameter(property = "versioning.version", defaultValue = "${project.version}", required = true)
    String version;

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

    String versionKind(String versionString) {
        VersionComponentScanner scanner = VersionComponentScanner.createInstance(versionString);
        scanner.getNextComponentInstance();
        VersionComponent selectedSuffix = scanner.getNextComponentInstance().component();
        if (selectedSuffix.isFinal() && !scanner.hasMoreComponents())
            return "final";
        else if (!selectedSuffix.isSpecial()) {
            return "other";
        } else {
            VersionComponent.SpecialKind kind = selectedSuffix.specialKind();
            if (kind == VersionComponent.SpecialKind.ALPHA)
                return "alpha";
            else if (kind == VersionComponent.SpecialKind.BETA)
                return "beta";
            else if (kind == VersionComponent.SpecialKind.RC)
                return "rc";
            else
                return "other";
        }
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
