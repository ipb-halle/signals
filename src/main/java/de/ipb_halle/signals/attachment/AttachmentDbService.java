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
package de.ipb_halle.signals.attachment;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * DB service for attachments
 */

@Stateless
public class AttachmentDbService {


    @PersistenceContext(unitName = "signalsDB")
    private EntityManager em;

    private final Logger logger = LoggerFactory.getLogger(AttachmentDbService.class);

    /**
     * @param cmap query criteria
     * @return
     */
    public List<Attachment> load(Map<String, Object> cmap) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<AttachmentEntity> criteriaQuery = criteriaBuilder.createQuery(AttachmentEntity.class);
        Root<AttachmentEntity> root = criteriaQuery.from(AttachmentEntity.class);
        criteriaQuery.select(root);

        List<Predicate> predicates = new ArrayList<>();
        if (cmap.containsKey(Attachment.ANCESTOR_ID)) {
            predicates.add(criteriaBuilder.equal(root.get(Attachment.ANCESTOR_ID), cmap.get(Attachment.ANCESTOR_ID)));
        }
        if (cmap.containsKey(Attachment.ATTACHMENT_ID)) {
            predicates.add(criteriaBuilder.equal(root.get(Attachment.ATTACHMENT_ID), cmap.get(Attachment.ATTACHMENT_ID)));
        }
        if (cmap.containsKey(Attachment.ENTITY_ID)) {
            predicates.add(criteriaBuilder.equal(root.get(Attachment.ENTITY_ID), cmap.get(Attachment.ENTITY_ID)));
        }
        if (cmap.containsKey(Attachment.FIELD_ID)) {
            predicates.add(criteriaBuilder.equal(root.get(Attachment.FIELD_ID), cmap.get(Attachment.FIELD_ID)));
        }

        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));

        List<Attachment> result = new ArrayList<>();
        for (AttachmentEntity entity : em.createQuery(criteriaQuery).getResultList()) {
            Attachment attachment = new Attachment(entity);
            List<AttachmentRevision> revisions = loadRevisions(
                    attachment.getId(),
                    (boolean) cmap.getOrDefault(Attachment.LATEST_ONLY, Boolean.FALSE));
            attachment.addRevisions(revisions);
            for (AttachmentRevision revision : revisions) {
                attachment.addFiles(loadAttachmentFiles(revision.getId()));
            }
            result.add(attachment);
        }
        return result;
    }

    public Attachment loadLatestRevisionById(int id) {
        AttachmentEntity entity = this.em.find(AttachmentEntity.class, id);
        if (entity != null) {
            Attachment attachment = new Attachment(entity);
            attachment.addRevisions(loadRevisions(id, true));
            attachment.addFiles(loadAttachmentFiles(attachment.getLatestRevision().getId()));
            return attachment;
        }
        return null;
    }

    private List<AttachmentRevision> loadRevisions(int attachmentId, boolean latestOnly) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<AttachmentRevision> criteriaQuery = criteriaBuilder.createQuery(AttachmentRevision.class);
        Root<AttachmentRevision> root = criteriaQuery.from(AttachmentRevision.class);
        criteriaQuery.select(root);

        List<Predicate> predicates = new ArrayList<>();
        if (latestOnly) {
            Subquery<Integer> subquery = criteriaQuery.subquery(Integer.class);
            subquery.from(AttachmentRevision.class);
            subquery.select(criteriaBuilder.max(root.get(AttachmentRevision.ID)));
            subquery.where(criteriaBuilder.equal(root.get(AttachmentRevision.ID), subquery));
        }
        predicates.add(criteriaBuilder.equal(root.get(AttachmentRevision.ATTACHMENT_ID), attachmentId));
        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
        criteriaQuery.orderBy(criteriaBuilder.asc(root.get(AttachmentRevision.ID)));
        return em.createQuery(criteriaQuery).getResultList();
    }

    public List<AttachmentFile> loadAttachmentFiles(Integer revisionId) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<AttachmentFile> criteriaQuery = criteriaBuilder.createQuery(AttachmentFile.class);
        Root<AttachmentFile> root = criteriaQuery.from(AttachmentFile.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.equal(root.get(AttachmentFile.ATTACHMENT_REVISION), revisionId));
        return em.createQuery(criteriaQuery).getResultList();
    }

    public void save(Attachment attachment) {
        AttachmentEntity entity = this.em.merge(attachment.createEntity());
        attachment.setId(entity.getId());
        saveLatestRevision(attachment);
    }

    private void saveLatestRevision(Attachment attachment) {
        AttachmentRevision rev = attachment.getLatestRevision();
        rev.setAttachmentId(attachment.getId());
        Integer oldRevisionId = rev.getId();
        AttachmentRevision persistedRevision = this.em.merge(rev);
        rev.setId(persistedRevision.getId());
        Set<AttachmentFile> files = attachment.getFiles(oldRevisionId);
        for (AttachmentFile file : files) {
            file.setRevisionId(persistedRevision.getId());
            AttachmentFile persistedFile = this.em.merge(file);
            file.setId(persistedFile.getId());
        }
        attachment.addFiles(files);
        attachment.discard();
    }
}

