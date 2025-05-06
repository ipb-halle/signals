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

package de.ipb_halle.signals.sample;

import de.ipb_halle.signals.RuntimeConfig;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.entity.SignalsEntityDbService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public  class  SampleManagerTest {

    private SampleManager sampleManager;
    private SignalsEntityDbService signalsEntityDbService;
    private SampleProcessorBean sampleProcessorBean;
    private SampleDbService sampleDbService;
    private SampleRestService sampleRestService;
    private DynEnumManager dynEnumManager;

    @BeforeEach
    public void setUp() {
        sampleManager = new SampleManager();

        // mocks
        signalsEntityDbService = mock(SignalsEntityDbService.class);
        sampleProcessorBean = mock(SampleProcessorBean.class);
        sampleDbService = mock(SampleDbService.class);
        sampleRestService = mock(SampleRestService.class);
        dynEnumManager = mock(DynEnumManager.class);

        // inject mocks
        injectField(sampleManager, "signalsEntityDbService", signalsEntityDbService);
        injectField(sampleManager, "sampleProcessorBean", sampleProcessorBean);
        injectField(sampleManager, "sampleDbService", sampleDbService);
        injectField(sampleManager, "sampleRestService", sampleRestService);
        injectField(sampleManager, "dynEnumManager", dynEnumManager);
    }

    @Test
    public void testManageSamples_processesAllSamples() {
        // given
        Date start = new Date();
        Date end = new Date();
        Date[] dateRange = new Date[] {start, end};

        SignalsEntityDTO dto1 = new SignalsEntityDTO();
        dto1.setId("sample:1");
        SignalsEntityDTO dto2 = new SignalsEntityDTO();
        dto2.setId("sample:2");

        when(signalsEntityDbService.loadSE(anyMap())).thenReturn(List.of(dto1, dto2));

        // when
        sampleManager.manageSamples(dateRange);

        // then
        verify(signalsEntityDbService).loadSE(anyMap());
        verify(sampleProcessorBean).processSingleSample("sample:1");
        verify(sampleProcessorBean).processSingleSample("sample:2");
    }

    @Test
    public void testImportSample_executesExpectedFlow() {
        // given
        String sampleId = "sample:xyz123";
        RuntimeConfig config = new RuntimeConfig();

        SampleEntity mockEntity = mock(SampleEntity.class);
        when(sampleDbService.loadSampleEntityById(sampleId)).thenReturn(mockEntity);

        // when
        sampleManager.importSample(config, sampleId);

        // then
        verify(sampleDbService).loadSampleEntityById(sampleId);
        verify(sampleDbService).loadSamplePropertyValuesWithProperties(any(Sample.class));
        verify(sampleRestService).createNewSample(any(Sample.class));
    }

    // Helper for injecting fields via reflection
    private void injectField(Object target, String fieldName, Object value) {
        try {
            var field = SampleManager.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Reflection injection failed", e);
        }
    }
}
