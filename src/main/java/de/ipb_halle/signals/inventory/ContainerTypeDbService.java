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

import de.ipb_halle.signals.attachment.Attachment;
import de.ipb_halle.signals.attachment.AttachmentDbService;
import de.ipb_halle.signals.attachment.AttachmentEntity;
import de.ipb_halle.signals.field.FieldDbService;
import de.ipb_halle.signals.field.Field;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;


/**
 * Database service for container types
 */

@Stateless
public class ContainerTypeDbService {

    public final static String CONTAINER_TYPE_ID = "container_type_id";

    @Inject
    private AttachmentDbService attachmentService;

    @Inject
    private FieldDbService fieldService;

    @PersistenceContext(unitName = "signalsDB")
    private EntityManager em;

    /**
     * @param id the ContainerType Id
     * @return a list of Attatchment for that container type
     */
    private List<AttachmentEntity> loadAttachments(String id) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<ContainerTypeAttachment> criteriaQuery = builder.createQuery(ContainerTypeAttachment.class);
        Root<ContainerTypeAttachment> root = criteriaQuery.from(ContainerTypeAttachment.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get(CONTAINER_TYPE_ID), id));

        List<AttachmentEntity> result = new ArrayList<>();
        for (ContainerTypeAttachment cta : em.createQuery(criteriaQuery).getResultList()) {
            result.add(attachmentService.loadById(cta.getAttachmentId()));
        }
        return result;
    }

    /**
     * @param id the ContainerType Id
     * @return a list of Field for that container type
     */
    private List<Field> loadFields(String id) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<ContainerTypeField> criteriaQuery = builder.createQuery(ContainerTypeField.class);
        Root<ContainerTypeField> root = criteriaQuery.from(ContainerTypeField.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get("id").get("id"), id));

        List<Field> result = new ArrayList();
        for (ContainerTypeField fd : em.createQuery(criteriaQuery).getResultList()) {
            result.add(fieldService.loadById(fd.getFieldId()));
        }
        return result;
    }

    public ContainerType loadById(String id) {
        ContainerTypeEntity cte = this.em.find(ContainerTypeEntity.class, id);
        return new ContainerType(cte,
                loadAttachments(cte.getId()),
                loadFields(cte.getId()));
    }

    public void save(ContainerType ct) {
        ContainerTypeEntity cte = ct.createEntity();
        this.em.merge(cte);
        for (AttachmentEntity a : ct.getAttachments()) {
            attachmentService.save(a);
            ContainerTypeAttachment cta = new ContainerTypeAttachment()
                    .setContainerTypeId(cte.getId())
                    .setAttachmentId(a.getId());
            em.merge(cta);
            attachmentService.save(a);
        }
        for (Field fd : ct.getFields()) {
            fieldService.save(fd);
            ContainerTypeField ctfd = new ContainerTypeField()
                    .setContainerTypeId(cte.getId())
                    .setFieldId(fd.getId());
            em.merge(ctfd);
        }
    }
}


