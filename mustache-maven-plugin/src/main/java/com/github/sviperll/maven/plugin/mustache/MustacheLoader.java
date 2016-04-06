/*
 * Copyright 2015 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.maven.plugin.mustache;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.apache.maven.plugin.logging.Log;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
class MustacheLoader {
    private final MustacheFactory mustacheFactory;
    private final Log logger;

    public MustacheLoader(MustacheFactory mustacheFactory, Log logger) {
        this.mustacheFactory = mustacheFactory;
        this.logger = logger;
    }

    Mustache load(File file, Charset charset) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(bufferedInputStream, charset);
                try {
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    try {
                        String fileCanonicalPath = file.getCanonicalPath().replace('\\', '/');
                        return mustacheFactory.compile(bufferedReader, fileCanonicalPath);
                    } finally {
                        try {
                            bufferedReader.close();
                        } catch (IOException ex) {
                            logger.error("Error closing bufferedReader", ex);
                        } catch (RuntimeException ex) {
                            logger.error("Error closing bufferedReader", ex);
                        }
                    }
                } finally {
                    try {
                        inputStreamReader.close();
                    } catch (IOException ex) {
                        logger.error("Error closing inputStreamReader", ex);
                    } catch (RuntimeException ex) {
                        logger.error("Error closing inputStreamReader", ex);
                    }
                }
            } finally {
                try {
                    bufferedInputStream.close();
                } catch (IOException ex) {
                    logger.error("Error closing bufferedInputStream", ex);
                } catch (RuntimeException ex) {
                    logger.error("Error closing bufferedInputStream", ex);
                }
            }
        } finally {
            try {
                fileInputStream.close();
            } catch (IOException ex) {
                logger.error("Error closing fileInputStream", ex);
            } catch (RuntimeException ex) {
                logger.error("Error closing fileInputStream", ex);
            }
        }
    }

}
