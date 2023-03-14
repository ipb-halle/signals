/*
 * IPB Signals client
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.ipb_halle.signals;

import jakarta.ejb.Stateless;

import org.apache.logging.log4j.Level;
//import org.apache.logging.log4j.Marker;
//import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.SmtpAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.filter.MarkerFilter;
import org.apache.logging.log4j.core.layout.HtmlLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/** 
 * Logger configuration
 */
@Stateless
public class LogConfig {

    private final static String emailMarkerTag = "EMAIL";
    private final static String emailAppenderName = "EMAIL";
    private final static int emailDefaultBufferSize = 10;

    private static Marker emailMarker = MarkerFactory.getMarker(emailMarkerTag);

    private Logger logger = LoggerFactory.getLogger(LogConfig.class);

    public void configureEmailLogging(SignalsConfig signalsConfig) {
/*
        WORK IN  PROGRESS

        Configuration config = LoggerContext
                                .getContext()
                                .getConfiguration();

        SmtpAppender.Builder builder = SmtpAppender.newBuilder();
        builder.setFrom("somebody@somewhere.invalid");
        builder.setTo("somebody@somewhere.invalid");
        builder.setSubject("TEST Signals Tool Logging");
        builder.setName(emailAppenderName);
        builder.setConfiguration(config);
        builder.setBufferSize(emailDefaultBufferSize);
        builder.setLayout(HtmlLayout.createDefaultLayout());
        builder.setFilter(MarkerFilter.createFilter(
                    emailMarkerTag, Filter.Result.ACCEPT, Filter.Result.DENY));

//      config.addAppender(builder.build());

        config.getLoggerConfig("de.ipb_halle").addAppender(builder.build(), Level.INFO, null);
*/
        return;
    }

    public static Marker getEmailMarker() {
        return emailMarker;
    }

    /**
     * @param userLevel text representation of the log level, should be one of 
     * <code>FATAL, ERROR, WARN, INFO, DEBUG, TRACE</code>.
     * @return true if setting of log level succeeded, false otherwise
     */
    public boolean setLogLevel(String userLevel) {
        try {
            Level level = Level.valueOf(userLevel);
            Configurator.setLevel("de.ipb_halle", level);
            return true;
        } catch (IllegalArgumentException iae) {
            logger.warn("Undefined level '{}' in setLogLevel()", userLevel);
        } catch (NullPointerException npe) {
            logger.warn("setLogLevel(userLevel) called with null argument");
        }
        return false;
    }

}


