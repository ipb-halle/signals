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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/** 
 * Container entity
 */

@Entity
@Table(name="containers")
public class ContainerEntity {

    @Id
    private String id;

    @Column
    private String barcode;

    @Column(name="json_string")
    private String jsonString;


    public String getBarcode() {
        return barcode;
    }

    public String getId() {
        return id;
    }

    public String getJsonString() {
        return jsonString;
    }

    public ContainerEntity setBarcode(String b) {
        barcode = b;
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
}
