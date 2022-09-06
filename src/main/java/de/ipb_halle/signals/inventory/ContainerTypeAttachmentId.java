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

import java.io.Serializable;
import javax.persistence.Embeddable;

/** 
 * SNB role privileges
 */
@Embeddable
public class ContainerTypeAttachmentId implements Serializable {
    private final static long serialVersionUID = 1L;

    private String container_type_id;

    private String attachment_id;


    public String getContainerTypeId() { 
        return container_type_id; 
    }

    public String getAttachmentId() {
        return attachment_id;
    }

    public void setContainerTypeId(String id) {
        container_type_id = id;
    }

    public void setAttachmentId(String id) {
        attachment_id = id;
    }
}
