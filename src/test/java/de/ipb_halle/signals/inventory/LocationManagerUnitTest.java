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

import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.entity.SignalsEntityDbService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LocationManagerUnitTest {

    @InjectMocks
    private LocationManager locationManager;

    @Mock
    private LocationDbService locationDbService;

    @Mock
    private LocationTypeDbService locationTypeDbService;

    @Mock
    private LocationRestService locationRestService;

    @Mock
    private SignalsEntityDbService signalsEntityDbService;

    @Test
    public void testImportLocation_shouldCallRestServiceIfUpdateSNBTrue() {
        RuntimeConfig config = new RuntimeConfig();
        config.updateSNB = true;

        LocationEntity entity = new LocationEntity();
        entity.setId("loc:123");
        entity.setTypeId("lt:456");
        entity.setFieldValues(new HashSet<>());

        LocationType type = new LocationType();
        type.setId("lt:456");

        when(locationDbService.loadLocationById("loc:123")).thenReturn(entity);
        when(locationTypeDbService.loadById("lt:456")).thenReturn(type);

        locationManager.importLocation(config, "loc:123");

        verify(locationRestService).doCreateLocation(eq(type), any(Location.class));
    }

    @Test
    public void testManagerLocations_shouldProcessOnlyUnknownIds() {

        Date startDate = new Date(System.currentTimeMillis() - 86400000L);
        Date endDate = new Date();
        Date[] dateRange = new Date[]{startDate, endDate};

        Set<String> knownTypeIds = Set.of("loc:known");

        SignalsEntityDTO dto1 = new SignalsEntityDTO();
        dto1.setId("loc:unknown1");

        SignalsEntityDTO dto2 = new SignalsEntityDTO();
        dto2.setId("loc:known");

        List<SignalsEntityDTO> entities = List.of(dto1, dto2);

        when(locationTypeDbService.getLocationTypIds()).thenReturn(knownTypeIds);
        when(signalsEntityDbService.loadSE(anyMap())).thenReturn(entities);

        LocationProcessorBean mockProcessor = mock(LocationProcessorBean.class);
        injectIntoField(locationManager, "locationProcessorBean", mockProcessor);

        locationManager.manageLocations(dateRange);

        verify(mockProcessor, times(1)).processSingleLocation("loc:unknown1");
        verify(mockProcessor, never()).processSingleLocation("loc:known");
    }

    private void injectIntoField(Object target, String fieldName, Object toInject) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, toInject);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject into field: " + fieldName, e);
        }
    }

}
