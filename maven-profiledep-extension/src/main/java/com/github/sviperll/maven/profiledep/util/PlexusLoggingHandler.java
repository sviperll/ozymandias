/*
 * Copyright (c) 2015, vir
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
package com.github.sviperll.maven.profiledep.util;

import java.text.MessageFormat;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.codehaus.plexus.logging.Logger;

/**
 *
 * @author vir
 */
public class PlexusLoggingHandler extends Handler {
    private final Logger logger;

    public PlexusLoggingHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void publish(LogRecord record) {
        Level level = record.getLevel();
        String message = MessageFormat.format(record.getMessage(), record.getParameters());
        Throwable throwable = record.getThrown();
        if (level.equals(Level.CONFIG)) {
            if (throwable != null) {
                logger.info(message, throwable);
            } else {
                logger.info(message);
            }
        } else if (level.equals(Level.FINE)) {
            if (throwable != null) {
                logger.debug(message, throwable);
            } else {
                logger.debug(message);
            }
        } else if (level.equals(Level.FINER)) {
            if (throwable != null) {
                logger.debug(message, throwable);
            } else {
                logger.debug(message);
            }
        } else if (level.equals(Level.FINEST)) {
            if (throwable != null) {
                logger.debug(message, throwable);
            } else {
                logger.debug(message);
            }
        } else if (level.equals(Level.INFO)) {
            if (throwable != null) {
                logger.info(message, throwable);
            } else {
                logger.info(message);
            }
        } else if (level.equals(Level.SEVERE)) {
            if (throwable != null) {
                logger.fatalError(message, throwable);
            } else {
                logger.fatalError(message);
            }
        } else if (level.equals(Level.WARNING)) {
            if (throwable != null) {
                logger.warn(message, throwable);
            } else {
                logger.warn(message);
            }
        } else {
            logger.warn("Log message with unsupported level: " + level);
            if (throwable != null) {
                logger.warn(message, throwable);
            } else {
                logger.warn(message);
            }
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
    
}
