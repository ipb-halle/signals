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


import java.util.*;

/**
 * Attachment DTO
 */
public class Attachment implements IAttachment {

    public final static String ATTR_CREATED_AT = "createdAt";

    public final static String ANCESTOR_ID = "ancestorId";
    public final static String ATTACHMENT_ID = "id";
    public final static String ENTITY_ID = "entityId";
    public final static String FIELD_ID = "fieldId";

    public final static String LATEST_ONLY = "latestRevision";
    public final static String STAGING = "staging";


    private Integer id;
    private String entityId;
    private String fieldId;
    private String ancestorId;

    private List<AttachmentRevision> revisions;
    private Map<Integer, Set<AttachmentFile>> files;

    public Attachment() {
        revisions = new ArrayList<>();
    }

    public Attachment(AttachmentEntity e) {
        this.id = e.getId();
        this.entityId = e.getEntityId();
        this.fieldId = e.getFieldId();
        this.ancestorId = e.getAncestorId();
        revisions = new ArrayList<>();
    }

    public AttachmentEntity createEntity() {
        AttachmentEntity attachmentEntity = new AttachmentEntity();
        attachmentEntity.setId(id);
        attachmentEntity.setEntityId(entityId);
        attachmentEntity.setFieldId(fieldId);
        attachmentEntity.setAncestorId(ancestorId);
        return attachmentEntity;
    }

    public void addRevisions(Collection<AttachmentRevision> revs) {
        revisions.addAll(revs);
    }

    public void addRevision(AttachmentRevision rev) {
        revisions.add(rev);
    }

    public void addFiles(Collection<AttachmentFile> fileList) {
        for (AttachmentFile file : fileList) {
            addFile(file);
        }
    }

    public void addFile(AttachmentFile file) {
        Set<AttachmentFile> revisionFiles = files.getOrDefault(
                file.getRevisionId(),
                new HashSet<AttachmentFile>());
        revisionFiles.add(file);
        files.put(file.getRevisionId(), revisionFiles);
    }

    /**
     * remove null keys of objects after persisting and assigning
     * an entity id
     */
    public void discard() {
        files.remove(null);
    }

    public List<AttachmentRevision> getRevisions() {
        return revisions;
    }

    public Set<AttachmentFile> getFiles(Integer revisionId) {
        return files.get(revisionId);
    }

    public AttachmentRevision getLatestRevision() {
        int numRevisions = revisions.size();
        if (numRevisions > 0) {
            return revisions.get(numRevisions - 1);
        }
        return null;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public IAttachment setId(Integer id) {
        this.id = id;
        revisions.forEach(rev -> rev.setAttachmentId(id));
        return this;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public String getAncestorId() {
        return ancestorId;
    }

    public void setAncestorId(String ancestorId) {
        this.ancestorId = ancestorId;
    }
}
