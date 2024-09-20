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


public class UpdateConfig {

    public boolean updateDb;

    public boolean updateSNB;

    public boolean updateFromLdap;

    public boolean syncDbFromSNB;

    public UpdateConfig() {
        this(true, true, true, true);
    }

    public UpdateConfig(boolean db, boolean snb, boolean ldap, boolean syncSNB) {
        updateDb = db;
        updateSNB = snb;
        updateFromLdap = ldap;
        syncDbFromSNB = syncSNB;
    }
}
