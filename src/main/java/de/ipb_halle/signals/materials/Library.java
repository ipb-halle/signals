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

import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.field.FieldDefinition;
import de.ipb_halle.signals.users.UserReference;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import de.ipb_halle.signals.users.IUser;

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
    
    public final static String LIBRARY_TYPE = "assetType";

    private String assetDisplayName;
    private Set<Field> assetFields;
    private String assetNameFieldId;
    private String assetNumberingFormat;

    private String batchDisplayName;
    private Set<Field> batchFields;
    private String batchNumberingFormat;

    private Date createdAt;
    private IUser createdBy;
    private String digest;
    private String displayImage;                // JSON
    private String displayTable;                // JSON
    private Date editedAt;
    private IUser editedBy;
    private Boolean enabled;
    private String entityFlags;                 // JSON

    private String id;
    private String materialsSampleMapping;      // JSON
    private String name;
    private String uniqueness;                  // JSON
    
    /**
     * default constructor
     */
    public Library() {
        assetFields = new HashSet<> ();
        batchFields = new HashSet<> ();
    }
    /*
     * public Library (LibraryEntity le, List<Field> assetFD, List<Field> batchFD)
     */
    public Library (LibraryEntity le) {
        id = le.getId();

        assetDisplayName = le.getAssetDisplayName();
        assetNameFieldId = le.getAssetNameFieldId();
        assetNumberingFormat = le.getAssetNumberingFormat() ;
        batchDisplayName = le.getBatchDisplayName();
        batchNumberingFormat = le.getBatchNumberingFormat() ;
        createdAt = le.getCreatedAt();
        createdBy = new UserReference(le.getCreatedBy());
        digest = le.getDigest();
        displayImage = le.getDisplayImage();
        displayTable = le.getDisplayTable();
        editedAt = le.getEditedAt();
        editedBy = new UserReference(le.getEditedBy());
        enabled = le.isEnabled();
        entityFlags = le.getEntityFlags();
        materialsSampleMapping = le.getMaterialsSampleMapping();
        name = le.getName();
        uniqueness = le.getUniqueness();

        /* complex types */
        assetFields = new HashSet<> ();
        batchFields = new HashSet<> ();
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
            .setCreatedBy(createdBy.getId())
            .setDigest(digest)
            .setDisplayImage(displayImage)
            .setDisplayTable(displayTable)
            .setEditedAt(editedAt)
            .setEditedBy(editedBy.getId())
            .setEnabled(enabled)
            .setEntityFlags(entityFlags)
            .setMaterialsSampleMapping(materialsSampleMapping)
            .setName(name)
            .setUniqueness(uniqueness);

        return entity;
    }

    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Library(%s): %s\n", id, name));
        return sb.toString();
    }

    public Library addAllAssetFields(Collection fields) {
        assetFields.addAll(fields);
        return this;
    }

    public Library addAllBatchFields(Collection fields) {
        batchFields.addAll(fields);
        return this;
    }

    public void addAssetField(Field f) {
        assetFields.add(f);
    }

    public void addBatchField(Field f) {
        batchFields.add(f);
    }

    public String getAssetDisplayName() {
        return assetDisplayName;
    }

    public Set<Field> getAssetFields() {
        return assetFields;
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

    public Set<Field> getBatchFields() {
        return batchFields;
    }

    public String getBatchNumberingFormat()  {
        return batchNumberingFormat;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public IUser getCreatedBy() {
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

    public IUser getEditedBy() {
        return editedBy;
    }

    public String getEntityFlags() {
        return entityFlags;
    }

    public String getEId() { return LIBRARY_TYPE + ":" + id; }

    public String getId() {
        return id;
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

    public Library setAssetFields(Set<Field> fields) {
        assetFields = fields;
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

    public Library setBatchFields(Set<Field> fields) {
        batchFields = fields;
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

    public Library setCreatedBy(IUser u) {
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

    public Library setEditedBy(IUser u) {
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
