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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

    private final Logger logger = LoggerFactory.getLogger(DynEnumManager.class);

    @Inject
    private DynEnumDbService dbService;

    private boolean discoverEnums;

    private Map<String, Map<String, DynEnum>> dynEnumsMapByType;
    private Map<Integer, DynEnum> dynEnumsById;

    /**
     * default constructor
     */
    public DynEnumManager() {
        discoverEnums = false;
        dynEnumsMapByType = new HashMap<>();
        dynEnumsById = new HashMap<>();
    }

    @PostConstruct
    public void init() {
        for (DynEnum de : dbService.load()) {
            putDynEnum(de);
        }
    }

    public void allowEnumDiscovery() {
        discoverEnums = true;
    }

    public DynEnum valueOf(DynEnum dynEnum) {
        String type = dynEnum.getShortType();
        if (dynEnumsMapByType.containsKey(type)) {
            DynEnum e = dynEnumsMapByType.get(type).get(dynEnum.getValue());
            if (e != null) {
                return e;
            }
        }
        return registerNewEnum(dynEnum);
    }

    public DynEnum valueOf(Integer id) {
        return dynEnumsById.get(id);
    }

    private DynEnum registerNewEnum(DynEnum dynEnum) {
        if (discoverEnums) {
            DynEnum de = dbService.save(dynEnum);
            putDynEnum(de);
            return de;
        }
        throw new RuntimeException("Discovered new DynEnum "
                + dynEnum.toString()
                + " when auto-discovery was disabled.");
    }

    private void putDynEnum(DynEnum de) {
        String type = de.getShortType();
        Map<String, DynEnum> typeMap = dynEnumsMapByType
                .getOrDefault(type, new HashMap<String, DynEnum>());
        typeMap.put(de.getValue(), de);
        dynEnumsMapByType.put(type, typeMap);
        dynEnumsById.put(de.getId(), de);
    }

    public List<Integer> getDynEnumIds(DynEnum[] dynEnums) {
        Integer[] idList = new Integer[dynEnums.length];
        for (int i = 0; i < dynEnums.length; i++) {
            idList[i] = valueOf(dynEnums[i]).getId();
        }
        return Arrays.asList(idList);
    }
}
