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
package de.ipb_halle.signals.materials;

import de.ipb_halle.signals.entity.FieldDefinition;
import java.util.Date;
import java.util.Set;

/** 
 * DTO material libraries 
 */

public class Library {

    public final static String ATTR_ASSETS = "assets";
    public final static String ATTR_ASSET_UNIQUENESS = "uniqueness";
    public final static String ATTR_ASSET_NAME_FIELD_ID = "assetNameFieldId";
    public final static String ATTR_ASSET_DISPLAY_NAME = "displayName";
    public final static String ATTR_ASSET_FIELDS = "fields";
    public final static String ATTR_ASSET_NUMBERING = "numbering";
    public final static String ATTR_BATCHES = "batches";
    public final static String ATTR_BATCH_DISPLAY_NAME = "displayName";
    public final static String ATTR_BATCH_FIELDS = "fields";
    public final static String ATTR_BATCH_NUMBERING = "numbering"; 
    public final static String ATTR_DISPLAY_IMAGE = "displayImage";
    public final static String ATTR_ENABLED = "enabled";
    public final static String ATTR_CREATED = "created";
    public final static String ATTR_DIGEST = "digest";
    public final static String ATTR_EDITED = "edited";
    public final static String ATTR_DISPLAY_TABLE = "displayTable";
    public final static String ATTR_MATERIALS_SAMPLE_MAPPING = "materialsSampleMapping";
    public final static String ATTR_ENTITY_FLAGS = "entityFlags";
    

    private String id;

    private String assetDisplayName;
    private Set<FieldDefinition> assetFieldDefinitions;
    private String assetNameFieldId;
    private String assetNumbering;

    private String batchDisplayName;
    private Set<FieldDefinition> batchFieldDefinitions;
    private String batchNumbering;

    private Boolean enabled;

    private String jsonString;
    private String name;

    

    public void dump() {
        System.out.printf("Library(%s): ...\n", id);
        System.out.println(jsonString);
        System.out.println("============================================================");
    }

    public void addAssetFieldDefinition(FieldDefinition fd) {
        assetFieldDefinitions.add(fd);
    }

    public void addBatchFieldDefinition(FieldDefinition fd) {
        batchFieldDefinitions.add(fd);
    }

    public String getAssetDisplayName() {
        return assetDisplayName;
    }

    public Set<FieldDefinition> getAssetFieldDefinitions() {
        return assetFieldDefinitions;
    }

    public String getAssetNameFieldId() {
        return assetNameFieldId;
    }

    public String getAssetNumbering()  {
        return assetNumbering;
    }

    public String getBatchDisplayName() {
        return batchDisplayName;
    }

    public Set<FieldDefinition> getBatchFieldDefinitions() {
        return batchFieldDefinitions;
    }

    public String getBatchNumbering()  {
        return assetNumbering;
    }

    public String getId() {
        return id;
    }

    public String getJsonString() {
        return jsonString;
    }

    public String getName() {
        return name;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public Library setId(String i) {
        id = i;
        return this;
    }

    public Library setAssetDisplayName(String n) {
        assetDisplayName = n;
        return this;
    }

    public Library setAssetFieldDefinitions(Set<FieldDefinition> fields) {
        assetFieldDefinitions = fields;
        return this;
    }

    public Library setAssetNameFieldId(String i) {
        assetNameFieldId = i;
        return this;
    }

    public Library setAssetNumbering(String n)  {
        assetNumbering = n;
        return this;
    }

    public Library setBatchDisplayName(String n) {
        batchDisplayName = n;
        return this;
    }

    public Library setBatchFieldDefinitions(Set<FieldDefinition> fields) {
        batchFieldDefinitions = fields;
        return this;
    }

    public Library setBatchNumbering(String n)  {
        assetNumbering = n;
        return this;
    }

    public Library setEnabled(Boolean e) {
        enabled = e;
        return this;
    }

    public Library setJsonString(String j) {
        jsonString = j;
        return this;
    }

    public Library setName(String n) {
        name = n;
        return this;
    }

}
