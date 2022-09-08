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
import java.util.HashSet;
import java.util.List;
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
    public final static String ATTR_BATCHES = "batches";
    public final static String ATTR_BATCH_DISPLAY_NAME = "displayName";
    public final static String ATTR_BATCH_FIELDS = "fields";
    public final static String ATTR_DISPLAY_IMAGE = "displayImage";
    public final static String ATTR_ENABLED = "enabled";
    public final static String ATTR_ENTITY_FLAGS = "entityFlags";
    public final static String ATTR_NUMBERING_FORMAT = "numbering.format";
    public final static String ATTR_PATH_CREATED_AT = "created.at";
    public final static String ATTR_PATH_CREATED_BY = "created.by.data.id";
    public final static String ATTR_PATH_EDITED_AT = "edited.at";
    public final static String ATTR_PATH_EDITED_BY = "edited.by.data.id";
    public final static String ATTR_DISPLAY_TABLE = "displayTable";
    public final static String ATTR_MATERIALS_SAMPLE_MAPPING = "materialsSampleMapping";
    


    private String assetDisplayName;
    private Set<FieldDefinition> assetFieldDefinitions;
    private String assetNameFieldId;
    private String assetNumberingFormat;

    private String batchDisplayName;
    private Set<FieldDefinition> batchFieldDefinitions;
    private String batchNumberingFormat;

    private Date createdAt;
    private Integer createdBy;
    private String digest;
    private String displayImage;                // JSON
    private String displayTable;                // JSON
    private Date editedAt;
    private Integer editedBy;
    private Boolean enabled;
    private String entityFlags;                 // JSON

    private String id;
    private String jsonString;                  // JSON
    private String materialsSampleMapping;      // JSON
    private String name;
    private String uniqueness;                  // JSON
    
    /**
     * default constructor
     */
    public Library() {
        assetFieldDefinitions = new HashSet<> ();
        batchFieldDefinitions = new HashSet<> ();
    }

    public Library (LibraryEntity le, List<FieldDefinition> assetFD, List<FieldDefinition> batchFD) {
        id = le.getId();

        assetDisplayName = le.getAssetDisplayName();
        assetNameFieldId = le.getAssetNameFieldId();
        assetNumberingFormat = le.getAssetNumberingFormat() ;
        batchDisplayName = le.getBatchDisplayName();
        batchNumberingFormat = le.getBatchNumberingFormat() ;
        createdAt = le.getCreatedAt();
        createdBy = le.getCreatedBy();
        digest = le.getDigest();
        displayImage = le.getDisplayImage();
        displayTable = le.getDisplayTable();
        editedAt = le.getEditedAt();
        editedBy = le.getEditedBy();
        enabled = le.isEnabled();
        entityFlags = le.getEntityFlags();
        jsonString = le.getJsonString();
        materialsSampleMapping = le.getMaterialsSampleMapping();
        name = le.getName();
        uniqueness = le.getUniqueness();

        assetFieldDefinitions = new HashSet<> ();
        assetFieldDefinitions.addAll(assetFD);
        batchFieldDefinitions = new HashSet<> ();
        batchFieldDefinitions.addAll(batchFD);
    }

    public LibraryEntity createEntity() {
        LibraryEntity entity = new LibraryEntity()
            .setId(id)
            .setAssetDisplayName(assetDisplayName)
            .setAssetNameFieldId(assetNameFieldId)
            .setAssetNumberingFormat(assetNumberingFormat)
            .setBatchDisplayName(batchDisplayName)
            .setBatchNumberingFormat(batchNumberingFormat)
            .setCreatedAt(createdAt)
            .setCreatedBy(createdBy)
            .setDigest(digest)
            .setDisplayImage(displayImage)
            .setDisplayTable(displayTable)
            .setEditedAt(editedAt)
            .setEditedBy(editedBy)
            .setEnabled(enabled)
            .setEntityFlags(entityFlags)
            .setJsonString(jsonString)
            .setMaterialsSampleMapping(materialsSampleMapping)
            .setName(name)
            .setUniqueness(uniqueness);

        return entity;
    }

    public void dump() {
        System.out.printf("Library(%s): %s\n", id, name);
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

    public String getAssetNumberingFormat()  {
        return assetNumberingFormat;
    }

    public String getBatchDisplayName() {
        return batchDisplayName;
    }

    public Set<FieldDefinition> getBatchFieldDefinitions() {
        return batchFieldDefinitions;
    }

    public String getBatchNumberingFormat()  {
        return batchNumberingFormat;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public String getDigest() {
        return digest;
    }

    public String getDisplayImage() {
        return displayImage;
    }

    public String getDisplayTable() {
        return displayTable;
    }

    public Date getEditedAt() {
        return editedAt;
    }

    public Integer getEditedBy() {
        return editedBy;
    }

    public String getEntityFlags() {
        return entityFlags;
    }

    public String getId() {
        return id;
    }

    public String getJsonString() {
        return jsonString;
    }

    public String getMaterialsSampleMapping() {
        return materialsSampleMapping;
    }

    public String getName() {
        return name;
    }

    public String getUniqueness() {
        return uniqueness;
    }

    public Boolean isEnabled() {
        return enabled;
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

    public Library setAssetNumberingFormat(String n)  {
        assetNumberingFormat = n;
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

    public Library setBatchNumberingFormat(String n)  {
        batchNumberingFormat = n;
        return this;
    }

    public Library setCreatedAt(Date d) {
        createdAt = d;
        return this;
    }

    public Library setCreatedBy(Integer u) {
        createdBy = u;
        return this;
    }

    public Library setDigest(String d) {
        digest = d;
        return this;
    }

    public Library setDisplayImage(String d) {
        displayImage = d;
        return this;
    }

    public Library setDisplayTable(String d) {
        displayTable = d;
        return this;
    }

    public Library setEditedAt(Date d) {
        editedAt = d;
        return this;
    }

    public Library setEditedBy(Integer u) {
        editedBy = u;
        return this;
    }

    public Library setEnabled(Boolean e) {
        enabled = e;
        return this;
    }

    public Library setEntityFlags(String f) {
        entityFlags = f;
        return this;
    }

    public Library setId(String i) {
        id = i;
        return this;
    }

    public Library setJsonString(String j) {
        jsonString = j;
        return this;
    }

    public Library setMaterialsSampleMapping(String m) {
        materialsSampleMapping = m;
        return this;
    }

    public Library setName(String n) {
        name = n;
        return this;
    }

    public Library setUniqueness(String u) {
        uniqueness = u;
        return this;
    }
}
