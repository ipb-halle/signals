/*
 *
 *  * IPB Signals client
 *  * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package de.ipb_halle.signals.inventory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContainerTypeDbServiceTest {

    private ContainerTypeDbService service;
    private EntityManager emMock;
    private CriteriaBuilder cbMock;
    private CriteriaQuery<ContainerTypeEntity> cqMock;
    private Root<ContainerTypeEntity> rootMock;
    private TypedQuery<ContainerTypeEntity> queryMock;

    @BeforeEach
    public void setup() {
        emMock = mock(EntityManager.class);
        cbMock = mock(CriteriaBuilder.class);
        cqMock = mock(CriteriaQuery.class);
        rootMock = mock(Root.class);
        queryMock = mock(TypedQuery.class);

        service = new ContainerTypeDbService();
        var field = ContainerTypeDbService.class.getDeclaredFields();
        for (var f : field) {
            if (f.getType().equals(EntityManager.class)) {
                f.setAccessible(true);
                try {
                    f.set(service, emMock);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
        }

        when(emMock.getCriteriaBuilder()).thenReturn(cbMock);
        when(cbMock.createQuery(ContainerTypeEntity.class)).thenReturn(cqMock);
        when(cqMock.from(ContainerTypeEntity.class)).thenReturn(rootMock);
        when(emMock.createQuery(cqMock)).thenReturn(queryMock);
    }

    @Test
    public void testGetContainerTypeIds_withValidResult() {
        ContainerTypeEntity entity1 = new ContainerTypeEntity();
        entity1.setId("type:mock-1");
        ContainerTypeEntity entity2 = new ContainerTypeEntity();
        entity2.setId("type:mock-2");

        when(queryMock.getResultList()).thenReturn(List.of(entity1, entity2));

        Set<String> result = service.getContainerTypeIds();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("type:mock-1"));
        assertTrue(result.contains("type:mock-2"));
    }

    @Test
    public void testGetContainerTypeIds_withNullId() {
        ContainerTypeEntity entity1 = new ContainerTypeEntity(); // no ID set
        when(queryMock.getResultList()).thenReturn(List.of(entity1));

        Set<String> result = service.getContainerTypeIds();

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Null IDs should not be added to the result set");
    }

    @Test
    public void testGetContainerTypeIds_emptyList() {
        when(queryMock.getResultList()).thenReturn(List.of());

        Set<String> result = service.getContainerTypeIds();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetContainerTypeIds_withException() {
        when(queryMock.getResultList()).thenThrow(new RuntimeException("DB error"));

        Set<String> result = service.getContainerTypeIds();

        assertNotNull(result);
        assertTrue(result.isEmpty(), "On exception, result should be empty but not null");
    }
}
