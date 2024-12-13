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

import de.ipb_halle.signals.config.LocalConfig;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/** 
 * DB service for attachments
 */

@Stateless
public class AttachmentDbService {


    @PersistenceContext(unitName="signalsDB")
    private EntityManager em;

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
        CriteriaQuery<AttachmentRevision> select = criteriaQuery.select(root);

        List<Predicate> predicates = new ArrayList<>();
        if (latestOnly) {
            Subquery<Integer> subquery = criteriaQuery.subquery(Integer.class);
            subquery.select(criteriaBuilder.max(root.get(AttachmentRevision.ID)));
            predicates.add(criteriaBuilder.equal(root.get(AttachmentRevision.ID), subquery));
        }
        predicates.add(criteriaBuilder.equal(root.get(AttachmentRevision.ATTACHMENT_ID), attachmentId));
        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
        return em.createQuery(criteriaQuery).getResultList();
    }

    public List<AttachmentFile> loadAttachmentFiles(Integer revisionId) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<AttachmentFile> criteriaQuery = criteriaBuilder.createQuery(AttachmentFile.class);
        Root<AttachmentFile> root = criteriaQuery.from(AttachmentFile.class);
        CriteriaQuery<AttachmentFile> select = criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.equal(root.get(AttachmentFile.ATTACHMENT_REVISION), revisionId));
        return em.createQuery(criteriaQuery).getResultList();
    }

    public void save(Attachment a) {
        this.em.merge(a.createEntity());
        saveRevisions(a.getRevisions());
        saveFiles(a);
    }

    private void saveRevisions(List<AttachmentRevision> revisions) {
        revisions.forEach(r -> this.em.merge(r));
    }

    private void saveFiles(Attachment attachment) {
        attachment.getRevisions().forEach(r -> saveFiles(attachment.getFiles(r.getId())));
    }

    private void saveFiles(Collection<AttachmentFile> files) {
        if ((files != null) && (!files.isEmpty())) {
            files.forEach(f -> this.em.merge(f));
        }
    }
}

