/*
 * Copyright 2015 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.maven.plugin.mustache;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;
import org.apache.maven.plugin.logging.Log;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class JsonLoader {
    private final ObjectMapper mapper;
    private final Log logger;

    JsonLoader(ObjectMapper mapper, Log logger) {
        this.mapper = mapper;
        this.logger = logger;
    }

    Object load(File file, Charset charset) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(bufferedInputStream, charset);
                try {
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    try {
                        return mapper.readValue(bufferedReader, Object.class);
                    } finally {
                        try {
                            bufferedReader.close();
                        } catch (Exception ex) {
                            logger.error("Error closing bufferedReader", ex);
                        }
                    }
                } finally {
                    try {
                        inputStreamReader.close();
                    } catch (Exception ex) {
                        logger.error("Error closing inputStreamReader", ex);
                    }
                }
            } finally {
                try {
                    bufferedInputStream.close();
                } catch (Exception ex) {
                    logger.error("Error closing bufferedInputStream", ex);
                }
            }
        } finally {
            try {
                fileInputStream.close();
            } catch (Exception ex) {
                logger.error("Error closing fileInputStream", ex);
            }
        }
    }

}
