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

public class LocationTypeDbServiceTest {

    private LocationTypeDbService service;
    private EntityManager emMock;
    private CriteriaBuilder cbMock;
    private CriteriaQuery<LocationTypeEntity> cqMock;
    private Root<LocationTypeEntity> rootMock;
    private TypedQuery<LocationTypeEntity> queryMock;

    @BeforeEach
    public void setup() {
        emMock = mock(EntityManager.class);
        cbMock = mock(CriteriaBuilder.class);
        cqMock = mock(CriteriaQuery.class);
        rootMock = mock(Root.class);
        queryMock = mock(TypedQuery.class);

        service = new LocationTypeDbService();
        injectEntityManager(service, emMock);

        when(emMock.getCriteriaBuilder()).thenReturn(cbMock);
        when(cbMock.createQuery(LocationTypeEntity.class)).thenReturn(cqMock);
        when(cqMock.from(LocationTypeEntity.class)).thenReturn(rootMock);
        when(emMock.createQuery(cqMock)).thenReturn(queryMock);
    }

    private void injectEntityManager(LocationTypeDbService service, EntityManager em) {
        try {
            var field = LocationTypeDbService.class.getDeclaredField("em");
            field.setAccessible(true);
            field.set(service, em);
        } catch (Exception e) {
            throw new RuntimeException("Could not inject EntityManager", e);
        }
    }

    @Test
    public void testGetLocationTypIds_withValidResult() {
        LocationTypeEntity entity1 = new LocationTypeEntity();
        entity1.setId("location:123");
        LocationTypeEntity entity2 = new LocationTypeEntity();
        entity2.setId("location:456");

        when(queryMock.getResultList()).thenReturn(List.of(entity1, entity2));

        Set<String> result = service.getLocationTypIds();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("location:123"));
        assertTrue(result.contains("location:456"));
    }

    @Test
    public void testGetLocationTypIds_withNullId() {
        LocationTypeEntity entity = new LocationTypeEntity(); // ID ist null
        when(queryMock.getResultList()).thenReturn(List.of(entity));

        Set<String> result = service.getLocationTypIds();

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Null-IDs dürfen nicht ins Resultat aufgenommen werden");
    }

    @Test
    public void testGetLocationTypIds_emptyResultList() {
        when(queryMock.getResultList()).thenReturn(List.of());

        Set<String> result = service.getLocationTypIds();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetLocationTypIds_withException() {
        when(queryMock.getResultList()).thenThrow(new RuntimeException("DB down"));

        Set<String> result = service.getLocationTypIds();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
