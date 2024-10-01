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
import de.ipb_halle.signals.field.FieldDefinition;
import de.ipb_halle.signals.field.FieldDefinitionDbService;
import java.util.ArrayList;
import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
//import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;


/** 
 * Database service for container types
 */

@Stateless
public class ContainerTypeDbService {

    public final static String FIELD_CONTAINER_TYPE_ID = "container_type_id";

    @Inject
    private AttachmentDbService attachmentService;

    @Inject 
    private FieldDefinitionDbService fieldService;

    @PersistenceContext(unitName="signalsDB")
    private EntityManager em;

    /**
     * @param id the ContainerType Id
     * @return a list of Attatchment for that container type
     */
    private List<Attachment> loadAttachments(String id) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<ContainerTypeAttachment> criteriaQuery = builder.createQuery(ContainerTypeAttachment.class);
        Root<ContainerTypeAttachment> root = criteriaQuery.from(ContainerTypeAttachment.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get(FIELD_CONTAINER_TYPE_ID), id));

        List<Attachment> result = new ArrayList<> ();
        for (ContainerTypeAttachment cta : em.createQuery(criteriaQuery).getResultList()) {
            result.add(attachmentService.loadById(cta.getAttachmentId()));
        }
        return result;
    }

    /**
     * @param id the ContainerType Id
     * @return a list of FieldDefinition for that container type
     */
    private List<FieldDefinition> loadFieldDefinitions(String id) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<ContainerTypeFieldDefinition> criteriaQuery = builder.createQuery(ContainerTypeFieldDefinition.class);
        Root<ContainerTypeFieldDefinition> root = criteriaQuery.from(ContainerTypeFieldDefinition.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get(FIELD_CONTAINER_TYPE_ID), id));

        List<FieldDefinition> result = new ArrayList<> ();
        for (ContainerTypeFieldDefinition fd : em.createQuery(criteriaQuery).getResultList()) {
            result.add(fieldService.loadById(fd.getFieldDefinitionId()));
        }
        return result;
    }

    public ContainerType loadById(String id) {
        ContainerTypeEntity cte = this.em.find(ContainerTypeEntity.class, id);
        return new ContainerType(cte, 
                loadAttachments(cte.getId()),
                loadFieldDefinitions(cte.getId()));
    }

    public void save(ContainerType ct) {
        ContainerTypeEntity cte = ct.createEntity();
        this.em.merge(cte);
        for (Attachment a : ct.getAttachments()) {
            attachmentService.save(a);
            ContainerTypeAttachment cta = new ContainerTypeAttachment()
                .setContainerTypeId(cte.getId())
                .setAttachmentId(a.getId());
            em.merge(cta);
            attachmentService.save(a);
        }
        for (FieldDefinition fd : ct.getFieldDefinitions()) {
            fieldService.save(fd);
            ContainerTypeFieldDefinition ctfd = new ContainerTypeFieldDefinition()
                .setContainerTypeId(cte.getId())
                .setFieldDefinitionId(fd.getId());
            em.merge(ctfd);
        }
    }
}


