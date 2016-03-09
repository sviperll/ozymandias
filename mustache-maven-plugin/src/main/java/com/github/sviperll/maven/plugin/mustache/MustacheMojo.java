/*
 * Copyright 2015 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.maven.plugin.mustache;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.Mustache;
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
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import org.apache.maven.plugin.AbstractMojo;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
abstract class MustacheMojo extends AbstractMojo {
    Map<String, Object> loadPropertiesContext(File contextFile, Charset charset) throws IOException {
        Properties properties = loadProperties(contextFile, charset);
        Map<String, Object> result = new TreeMap<String, Object>();
        MapFiller filler = new MapFiller(result);
        for (String key: properties.stringPropertyNames()) {
            filler.put(key, properties.getProperty(key));
        }
        return result;
    }

    Object loadJsonContext(File contextFile, Charset charset) throws IOException {
        JsonFactory jsonFactory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(jsonFactory);
        JsonLoader jsonLoader = new JsonLoader(mapper, getLog());
        return jsonLoader.load(contextFile, charset);
    }

    void renderMustache(Mustache mustache, Object context, File outputFile, Charset charset) throws FileNotFoundException {
        File outputDirectory = outputFile.getParentFile();
        if (outputDirectory != null)
            outputDirectory.mkdirs();
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        try {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(bufferedOutputStream, charset);
                try {
                    BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
                    try {
                        MustacheRenderer renderer = new MustacheRenderer(bufferedWriter);
                        renderer.render(mustache, context);
                    } finally {
                        try {
                            bufferedWriter.close();
                        } catch (Exception ex) {
                            getLog().error("Unable to close bufferedWriter", ex);
                        }
                    }
                } finally {
                    try {
                        outputStreamWriter.close();
                    } catch (Exception ex) {
                        getLog().error("Unable to close outputStreamWriter", ex);
                    }
                }
            } finally {
                try {
                    bufferedOutputStream.close();
                } catch (Exception ex) {
                    getLog().error("Unable to close bufferedOutputStream", ex);
                }
            }
        } finally {
            try {
                fileOutputStream.close();
            } catch (Exception ex) {
                getLog().error("Unable to close fileOutputStream", ex);
            }
        }
    }

    Properties loadProperties(File file, Charset charset) throws FileNotFoundException, IOException {
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
