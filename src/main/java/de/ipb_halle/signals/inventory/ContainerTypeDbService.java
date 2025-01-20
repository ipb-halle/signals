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

import de.ipb_halle.signals.attachment.AttachmentDbService;
import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.field.FieldDbService;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Database service for container types
 */

@Stateless
public class ContainerTypeDbService {

    public final static String CONTAINER_TYPE_ID = "container_type_id";
    private final static String CONTAINER_TYPE_ENTITY_PREFIX = "container:";
    private final static String CONTAINER_TYPE_ENTITY_SUFFIX = ":ivt";

    @Inject
    private AttachmentDbService attachmentService;

    @Inject
    private FieldDbService fieldService;

    @PersistenceContext(unitName = "signalsDB")
    private EntityManager em;


    public Set<String> getContainerTypeIds() {
        Set<String> entityIds = new HashSet<>();
        try {
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<ContainerTypeEntity> query = builder.createQuery(ContainerTypeEntity.class);
            Root<ContainerTypeEntity> root = query.from(ContainerTypeEntity.class);
            query.select(root);

            List<ContainerTypeEntity> resultList = em.createQuery(query).getResultList();
            System.out.println("Query result: " + resultList);

            if (resultList == null || resultList.isEmpty()) {
                System.err.println("ContainerTypeDbService:-> No results found for ContainerTypeEntity.");
                return entityIds;
            }

            for (ContainerTypeEntity cte : resultList) {
                if (cte.getId() != null) {
                    entityIds.add(CONTAINER_TYPE_ENTITY_PREFIX + cte.getId() + CONTAINER_TYPE_ENTITY_SUFFIX);
                } else {
                    System.err.println("ContainerTypeDbService:-> Null ID found for a ContainerTypeEntity.");
                }
            }
        } catch (Exception e) {
            System.err.println("ContainerTypeDbService:-> Error while fetching container type IDs: " + e.getMessage());
            e.printStackTrace();
        }
        return entityIds;
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
                // loadAttachments(cte.getId()),
                loadFields(cte.getId()));
    }

    public void save(ContainerType ct) {
        ContainerTypeEntity cte = ct.createEntity();
        this.em.merge(cte);
        /*
        for (AttachmentEntity a : ct.getAttachments()) {
            attachmentService.save(a);
            ContainerTypeAttachment cta = new ContainerTypeAttachment()
                    .setContainerTypeId(cte.getId());
                    .setAttachmentId(a.getId());
            em.merge(cta);
            attachmentService.save(a);
        }
         */
        for (Field fd : ct.getFields()) {
            fieldService.save(fd);
            ContainerTypeField ctfd = new ContainerTypeField()
                    .setContainerTypeId(cte.getId())
                    .setFieldId(fd.getId());
            em.merge(ctfd);
        }
    }
}


