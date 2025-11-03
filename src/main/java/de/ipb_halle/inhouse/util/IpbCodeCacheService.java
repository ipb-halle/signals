/*
 *
 *  * IPB Signals client
 *  * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package de.ipb_halle.inhouse.util;

import de.ipb_halle.signals.ado.Ado;
import de.ipb_halle.signals.ado.AdoDbService;
import de.ipb_halle.signals.ado.AdoRestService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.*;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * IPBCodeCacheService
 *
 * <p>Singleton service for caching of fixed List of ADO objects representing IPB-Code
 * it also determines IPB-Code selection according to modulo rule index = numeric(IPB) % bucketSize</p>
 *
 * <p>Multithreading security: ejb-container controls concurrency.
 * - Writing methods (initialization/refresh/clear) marked {@link jakarta.ejb.LockType#WRITE} as exclusive.
 * - Reading methods (pick/get) - {@link jakarta.ejb.LockType#READ} - can be processed simultaneously as long as no writing method started.
 * </p>
 */

@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@AccessTimeout(value = 5, unit = TimeUnit.MINUTES)
public class IpbCodeCacheService {

    private static final Logger log = LogManager.getLogger(IpbCodeCacheService.class);

    private static final int DEFAULT_BUCKET_SIZE = 10;

    @Inject
    AdoDbService adoDbService;

    @Inject
    AdoRestService adoRestService;

    private volatile List<Ado> cache = Collections.emptyList();
    private volatile String templateId;
    private volatile int bucketSize = DEFAULT_BUCKET_SIZE;

    @PostConstruct
    private void onStart() {
        log.info("IPBCodeCacheService started. Call initialize(templateId, ...) to prepare cache.");
    }

    @Lock(LockType.WRITE)
    public void initialize(String templateId,
                           int bucketSize,
                           Integer generateStartInclusive,
                           Integer generateEndInclusive,
                           boolean createIfMissing) {
        if (bucketSize <= 0) {
            throw new IllegalArgumentException("bucketSize must be > 0");
        }
        this.templateId = templateId;
        this.bucketSize = bucketSize;

        // 1) Load Ado from db sorted upon ipbCode
        List<Ado> fromDb = adoDbService.loadByTemplateIdSorted(templateId);

        // 2)
        if (fromDb.size() < bucketSize && createIfMissing) {
            if (generateStartInclusive == null || generateEndInclusive == null) {
                throw new IllegalStateException("Not enough ADo in DB (" + fromDb.size()
                        + "), but generation range not provided.");
            }
            List<Ado> created = adoRestService.createAdosRange(
                    generateStartInclusive, generateEndInclusive, templateId);
            adoDbService.saveAll(created);
            fromDb = adoDbService.loadByTemplateIdSorted(templateId);
        }
        if (fromDb.size() < bucketSize) {
            throw new IllegalStateException("Need at least " + bucketSize + " ADOs, found " + fromDb.size());
        }

        List<Ado> snap = new ArrayList<>(bucketSize);
        for (int i = 0; i < bucketSize; i++) {
            snap.add(fromDb.get(i));
        }
        this.cache = Collections.unmodifiableList(snap);
        log.info("IPBCodeCacheService initialized: templateId={}, buckets={}, cached={}", templateId, bucketSize, cache.size());
    }

    @Lock(LockType.WRITE)
    public void refreshFromDatabase() {
        ensureInitialized();
        List<Ado> fromDb = adoDbService.loadByTemplateIdSorted(templateId);
        if (fromDb.size() < bucketSize) {
            throw new IllegalStateException("refreshFromDataBase: not enough Ado in DB for bucketSize=" + bucketSize);
        }
        List<Ado> snap = new ArrayList<>(bucketSize);
        for (int i = 0; i < bucketSize; i++) {
            snap.add(fromDb.get(i));
        }
        this.cache = Collections.unmodifiableList(snap);
        log.info("IPBCodeCacheService refreshed from DB, size={}", cache.size());
    }

    @Lock(LockType.WRITE)
    public void clear() {
        this.cache = Collections.emptyList();
        this.templateId = null;
        this.bucketSize = DEFAULT_BUCKET_SIZE;
        log.info("IPBCodeCacheService cleared");
    }

    @Lock(LockType.READ)
    public List<Ado> getCachedAdos() {
        ensureInitialized();
        return cache;
    }

    /**
     * This is a main method choosing Ado upon String IPB-code.
     * <p> Uses {@link IpbCodeNormalizer} we normalizing the String ipbCode</p>
     */
    @Lock(LockType.READ)
    public Ado pickAdoForIpb(String rawIpb) {
        ensureInitialized();
        int number = IpbCodeNormalizer.extractNumeric(rawIpb)
                .orElseThrow(() -> new IllegalArgumentException("Invalid IPV Code: " + rawIpb));
        int idx = Math.floorMod(number, cache.size());
        return cache.get(idx);
    }

    @Lock(LockType.READ)
    public String getTemplateId() {
        ensureInitialized();
        return templateId;
    }

    @Lock(LockType.READ)
    public int getBucketSize() {
        ensureInitialized();
        return bucketSize;
    }


    private void ensureInitialized() {
        if (cache.isEmpty() || templateId == null) {
            throw new IllegalStateException("IPBCodeCacheService is not initialized.  Call initialize( ...) first");
        }
    }
}
