/*
 * IPB Signals client
 * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.signals.attachment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.nio.file.Path;
import java.util.Objects;

@Entity
@Table(name = "attachment_files")
public class AttachmentFile {

    public final static String ATTACHMENT_REVISION = "revisionId";

    @Id
    private Integer id;

    @Column(name = "revision_id")
    private Integer revisionId;

    @Column(name = "mime_type")
    private String mimeType;

    @Column
    private String digest;

    private transient Path tempPath;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(Integer revisionId) {
        this.revisionId = revisionId;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public Path getTempPath() {
        return tempPath;
    }

    public void setTempPath(Path tempPath) {
        this.tempPath = tempPath;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        AttachmentFile that = (AttachmentFile) object;
        return Objects.equals(id, that.id) && Objects.equals(mimeType, that.mimeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, mimeType);
    }
}
