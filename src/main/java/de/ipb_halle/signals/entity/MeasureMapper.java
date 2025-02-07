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
package de.ipb_halle.signals.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapping between Quality and SNB measure
 */

public class MeasureMapper{

    private static Map<String, Quality> map;

    static {
        map = new HashMap<> ();
        for (Quality q : Quality.values()) {
            map.put(q.getSnbMeasure().toUpperCase(), q);
        }
    }

    /**
     */
    public static Quality getQuality(String measure) {
        Quality q = map.get(measure.toUpperCase());
        if (q == null) {
            throw new NullPointerException(String.format("No mapping found for measure %s", measure));
        }
        return q;
    }
}
