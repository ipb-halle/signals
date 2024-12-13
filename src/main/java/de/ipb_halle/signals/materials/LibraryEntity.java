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

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** 
 * material library db entity
 */

@Entity
@Table(name="libraries")
public class LibraryEntity {

    @Id
    private String id;

    @Column(name="asset_display_name")
    private String assetDisplayName;

    @Column(name="asset_name_field_id")
    private String assetNameFieldId;

    @Column(name="asset_numbering_format")
    private String assetNumberingFormat;

    @Column(name="batch_display_name")
    private String batchDisplayName;

    @Column(name="batch_numbering")
    private String batchNumberingFormat;

    @Column(name="created_at")
    private Date createdAt;

    @Column(name="created_by")
    private String createdBy;

    @Column
    private String digest;

    @Column(name="display_image")
    private String displayImage;                // JSON

    @Column(name="display_table")
    private String displayTable;                // JSON

    @Column(name="edited_at")
    private Date editedAt;

    @Column(name="edited_by")
    private String editedBy;

    @Column
    private Boolean enabled;

    @Column(name="entity_flags")
    private String entityFlags;                 // JSON

    @Column(name="materials_sample_mapping")
    private String materialsSampleMapping;      // JSON

    @Column
    private String name;

    @Column
    private String uniqueness;                  // JSON
    
    public String getAssetDisplayName() {
        return assetDisplayName;
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

    public String getBatchNumberingFormat()  {
        return batchNumberingFormat;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
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

    public String getEditedBy() {
        return editedBy;
    }

    public String getEntityFlags() {
        return entityFlags;
    }

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

    public LibraryEntity setAssetDisplayName(String n) {
        assetDisplayName = n;
        return this;
    }

    public LibraryEntity setAssetNameFieldId(String i) {
        assetNameFieldId = i;
        return this;
    }

    public LibraryEntity setAssetNumberingFormat(String n)  {
        assetNumberingFormat = n;
        return this;
    }

    public LibraryEntity setBatchDisplayName(String n) {
        batchDisplayName = n;
        return this;
    }

    public LibraryEntity setBatchNumberingFormat(String n)  {
        batchNumberingFormat = n;
        return this;
    }

    public LibraryEntity setCreatedAt(Date d) {
        createdAt = d;
        return this;
    }

    public LibraryEntity setCreatedBy(String u) {
        createdBy = u;
        return this;
    }

    public LibraryEntity setDigest(String d) {
        digest = d;
        return this;
    }

    public LibraryEntity setDisplayImage(String d) {
        displayImage = d;
        return this;
    }

    public LibraryEntity setDisplayTable(String d) {
        displayTable = d;
        return this;
    }

    public LibraryEntity setEditedAt(Date d) {
        editedAt = d;
        return this;
    }

    public LibraryEntity setEditedBy(String u) {
        editedBy = u;
        return this;
    }

    public LibraryEntity setEnabled(Boolean e) {
        enabled = e;
        return this;
    }

    public LibraryEntity setEntityFlags(String f) {
        entityFlags = f;
        return this;
    }

    public LibraryEntity setId(String i) {
        id = i;
        return this;
    }

    public LibraryEntity setMaterialsSampleMapping(String m) {
        materialsSampleMapping = m;
        return this;
    }

    public LibraryEntity setName(String n) {
        name = n;
        return this;
    }

    public LibraryEntity setUniqueness(String u) {
        uniqueness = u;
        return this;
    }
}
