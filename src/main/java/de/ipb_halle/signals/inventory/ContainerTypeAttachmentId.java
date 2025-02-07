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
import java.util.Objects;

/**
 * Attachment to container type compound id. This class solely exists for
 * JPA purposes.
 */
public class ContainerTypeAttachmentId implements Serializable {
    private final static long serialVersionUID = 1L;

    private String container_type_id;

    private String attachment_id;

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        ContainerTypeAttachmentId other = (ContainerTypeAttachmentId) o;
        return Objects.equals(container_type_id, other.container_type_id)
            && Objects.equals(attachment_id, other.attachment_id);
    }

    @Override
    public int hashCode() {
        return container_type_id.hashCode() + attachment_id.hashCode();
    }
}
