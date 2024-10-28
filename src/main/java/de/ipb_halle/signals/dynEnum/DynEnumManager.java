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
package de.ipb_halle.signals.dynEnum;

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Database Service for DynEnums
 */

@Singleton(name = "dynEnumManager")
@Startup
public class DynEnumManager {

    private record DynEnumType(String type, String value) {};

    @Inject
    private DynEnumDbService dbService;

    private boolean discoverEnums;

    private Map<DynEnumType, DynEnum> dynEnums;
    private Map<Integer, DynEnum> dynEnumsById;

    private final Logger logger = LoggerFactory.getLogger(DynEnumManager.class);

    /**
     * default constructor
     */
    public DynEnumManager() {
        discoverEnums = false;
        dynEnums = new HashMap<> ();
        dynEnumsById = new HashMap<> ();
    }

    @PostConstruct
    public void init() {
        for (DynEnum de : dbService.load()) {
            dynEnums.put(new DynEnumType(
                    de.getShortType(),
                    de.getValue()),
                    de);
            dynEnumsById.put(de.getId(), de);
        }
    }

    public void allowEnumDiscovery() {
        discoverEnums = true;
    }

    public DynEnum valueOf(DynEnum dynEnum) {
        DynEnumType type = new DynEnumType(
                dynEnum.getShortType(),
                dynEnum.getValue());
        DynEnum e = dynEnums.get(type);
        if (e == null) {
            return registerNewEnum(dynEnum);
        }
        return e;
    }

    public DynEnum valueOf(Integer id) {
        return dynEnumsById.get(id);
    }

    private DynEnum registerNewEnum(DynEnum dynEnum) {
        if (discoverEnums) {
            DynEnum de = dbService.save(dynEnum);
            DynEnumType type = new DynEnumType(
                de.getShortType(),
                de.getValue());
            dynEnums.put(type, de);
            dynEnumsById.put(de.getId(), de);
            return de;
        }
        throw new RuntimeException("Discovered new DynEnum "
                + dynEnum.toString()
                + " when auto-discovery was disabled.");
    }
}
