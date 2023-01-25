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
package de.ipb_halle.signals.inventory;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** 
 * Container entity
 */

@Entity
@Table(name="containers")
public class ContainerEntity {

    @Id
    private String id;

    @Column
    private Double amount;

    @Column
    private String barcode;

    @Column(name="container_type_id")
    private String containerTypeId;

    @Column(name="coordinate_x")
    private Integer coordinateX;

    @Column(name="coordinate_y")
    private Integer coordinateY;

    @Column(name="created_at")
    private Date createdAt;

    @Column(name="created_by")
    private String createdBy;

    @Column
    private String digest;

    @Column(name="json_string")
    private String jsonString;

    @Column(name="location_id")
    private String locationId;

    @Column
    private String name;

    @Column
    private String unit;

    @Column(name="updated_at")
    private Date updatedAt;

    @Column(name="updated_by")
    private String updatedBy;


    public Double getAmount() {
        return amount;
    }

    public String getBarcode() {
        return barcode;
    }

    public Integer getCoordinateX() {
        return coordinateX;
    }

    public Integer getCoordinateY() {
        return coordinateY;
    }

    public String getContainerTypeId() {
        return containerTypeId;
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

    public String getId() {
        return id;
    }

    public String getJsonString() {
        return jsonString;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public ContainerEntity setAmount(Double a) {
        amount = a;
        return this;
    }

    public ContainerEntity setBarcode(String b) {
        barcode = b;
        return this;
    }

    public ContainerEntity setCoordinateX(Integer x) {
        coordinateX = x;
        return this;
    }

    public ContainerEntity setCoordinateY(Integer y) {
        coordinateY = y;
        return this;
    }

    public ContainerEntity setContainerTypeId(String i) {
        containerTypeId = i;
        return this;
    }

    public ContainerEntity setCreatedAt(Date d) {
        createdAt = d;
        return this;
    }

    public ContainerEntity setCreatedBy(String u) {
        createdBy = u;
        return this;
    }

    public ContainerEntity setDigest(String d) {
        digest = d;
        return this;
    }

    public ContainerEntity setId(String i) {
        id = i;
        return this;
    }

    public ContainerEntity setJsonString(String j) {
        jsonString = j;
        return this;
    }

    public ContainerEntity setLocationId(String i) {
        locationId = i;
        return this;
    }

    public ContainerEntity setName(String n) {
        name = n;
        return this;
    }

    public ContainerEntity setUnit(String u) {
        unit = u;
        return this;
    }

    public ContainerEntity setUpdatedAt(Date d) {
        updatedAt = d;
        return this;
    }

    public ContainerEntity setUpdatedBy(String u) {
        updatedBy = u;
        return this;
    }
}
