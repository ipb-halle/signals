/*
 * IPB Signals client
 * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateRangeParser {


    public static Logger logger = LoggerFactory.getLogger(DateRangeParser.class);

    /**
     * Parser for additional arguments option start and end
     */
    public static Date[] parseDateRange(String[] dateRange) throws ParseException {
        Date[] range = new Date[2];
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        range[1] = new Date();

        if ((dateRange == null) || (dateRange.length == 0) || dateRange[0].isEmpty()) {
            range[0] = getOneWeekAgoDate();
            logger.trace("Fetching last weeks data: {} - {}", range[0], range[1]);
            return range;
        }
        if (dateRange[0].equals("all")) {
            range[0] = new Date(0); // 1970-01-01
            logger.trace("Fetching ALL data");
            return range;
        }

        range[0] = dateFormat.parse(dateRange[0]);
        if ((dateRange.length > 1) && (!dateRange[1].isEmpty())) {
            range[1] = dateFormat.parse(dateRange[1]);
        }
        logger.trace("Fetching specified data range: {} - {}", range[0], range[1]);
        return range;
    }

    private static Date getOneWeekAgoDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, -1);
        return calendar.getTime();
    }

    /**
     * return a date "maximum" range, mainly for testing purposes
     * @return a date range starting on epoch (1970-01-01) until now.
     */
    public static Date[] getAllDateRange() {
        Date[] range = new Date[2];
        range[0] = new Date(0);
        range[1] = new Date();
        return range;
    }
}
