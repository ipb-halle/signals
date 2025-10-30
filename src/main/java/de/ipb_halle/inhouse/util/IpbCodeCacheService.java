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

import de.ipb_halle.signals.ado.AdoDbService;
import jakarta.ejb.*;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

}
