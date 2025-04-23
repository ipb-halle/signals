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

package de.ipb_halle.signals;

import de.ipb_halle.inhouse.InhouseDB;
import de.ipb_halle.signals.attribute.AttributeManager;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.element.ElementConfig;
import de.ipb_halle.signals.element.ElementManager;
import de.ipb_halle.signals.entity.SignalsEntityConfig;
import de.ipb_halle.signals.entity.SignalsEntityManager;
import de.ipb_halle.signals.experiments.ExperimentConfig;
import de.ipb_halle.signals.experiments.ExperimentManager;
import de.ipb_halle.signals.inventory.InventoryConfig;
import de.ipb_halle.signals.inventory.InventoryManager;
import de.ipb_halle.signals.materials.MaterialsConfig;
import de.ipb_halle.signals.materials.MaterialsManager;
import de.ipb_halle.signals.sample.SampleConfig;
import de.ipb_halle.signals.sample.SampleManager;
import de.ipb_halle.signals.users.AccessConfig;
import de.ipb_halle.signals.users.AccessManager;
import de.ipb_halle.signals.users.LdapClient;
import jakarta.ejb.embeddable.EJBContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import javax.naming.Context;
import javax.naming.NamingException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

public class SignalsTest {


    private Signals signals;
    private ElementConfig elementConfig;

    @BeforeEach
    public void setup() {
        signals = new Signals();
        injectMocks(signals);
        signals.postConstruct();
    }

    @Test
    void testPostConstruct_initializesConfigs() {
        assertNotNull(signals.getRuntimeConfig());
        assertNotNull(signals.getSignalsEntityConfig());
        assertNotNull(signals.getInventoryConfig());
        assertNotNull(signals.getMaterialsConfig());
        assertNotNull(signals.getSamplesConfig());
        assertNotNull(signals.getExperimentConfig());
    }

    @Test
    void testManageExperiments_shouldCallExperimentConfig() {
        var experimentConfig = mock(ExperimentConfig.class);
        inject(signals, "experimentConfig", experimentConfig);
        Date[] range = {new Date()};
        signals.manageExperiments(range);
        verify(experimentConfig).manageExperiments(range);
    }

    @Test
    void testManageMaterials_shouldCallMaterialsConfig() {
        var materialsConfig = mock(MaterialsConfig.class);
        inject(signals, "materialsConfig", materialsConfig);
        Date[] range = {new Date()};
        signals.manageMaterials(range);
        verify(materialsConfig).manageMaterials(range);
    }

    @Test
    void testDumpEntities_shouldCallDumpOnSignalsEntityConfig() {
        var config = mock(SignalsEntityConfig.class);
        inject(signals, "signalsEntityConfig", config);
        Date[] range = {new Date()};
        signals.dumpEntities(range);
        verify(config).dumpEntities(range);
    }

    @Test
    void testManageAccess_shouldDelegateToAccessConfig() {
        var accessConfig = mock(AccessConfig.class);
        inject(signals, "accessConfig", accessConfig);
        signals.manageAccess();
        verify(accessConfig).manageAccess();
    }

    @Test
    public void testManageElements_shouldDelegateToElementConfig() {
        // given
        signals.postConstruct();

        var mockElementConfig = mock(ElementConfig.class);
        inject(signals, "elementConfig", mockElementConfig);

        Date[] dates = new Date[] { new Date() };

        // when
        signals.manageElements(dates);

        // then
        verify(mockElementConfig).manageElements(dates);
    }

    @Test
    void testDumpSet_printsAllElements() {
        Set<String> input = new HashSet<>();
        input.add("First");
        input.add("Second");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));
        try {
            inject(signals, "logger", mock(org.slf4j.Logger.class)); // Suppress logger
            var method = Signals.class.getDeclaredMethod("dumpSet", Set.class);
            method.setAccessible(true);
            method.invoke(signals, input);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        } finally {
            System.setOut(originalOut);
        }

        String result = out.toString();
        assertTrue(result.contains("First") || result.contains("Second"));
    }

    @Test
    public void testGetters() {
        assertNotNull(signals.getAttributeManager());
        assertNotNull(signals.getDynEnumMgr());
        assertNotNull(signals.getInhouseDB());
        assertNotNull(signals.getMaterialsConfig());
        assertNotNull(signals.getSamplesConfig());
        assertNotNull(signals.getExperimentConfig());
        assertNotNull(signals.getInventoryConfig());
        assertNotNull(signals.getSignalsEntityConfig());
        assertNotNull(signals.getLogConfig());
    }

    @Test
    void testGetInstance_shouldInitializeSignals() {
        Signals signals = spy(new Signals());
        doNothing().when(signals).postConstruct();

        try (MockedStatic<EJBContainer> mockedContainer = mockStatic(EJBContainer.class)) {
            EJBContainer container = mock(EJBContainer.class);
            Context context = mock(Context.class);
            when(container.getContext()).thenReturn(context);
            mockedContainer.when(() -> EJBContainer.createEJBContainer(any(Properties.class))).thenReturn(container);

            doNothing().when(context).bind(eq("inject"), any(Signals.class));

            Signals instance = Signals.getInstance("mock-config.xml");
            assertNotNull(instance);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }





    private void inject(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void injectMocks(Signals signals) {
        inject(signals, "signalsConfig", mock(SignalsConfig.class));
        inject(signals, "attributeManager", mock(AttributeManager.class));
        inject(signals, "dynEnumMgr", mock(DynEnumManager.class));
        inject(signals, "signalsMgr", mock(SignalsEntityManager.class));
        inject(signals, "accessManager", mock(AccessManager.class));
        inject(signals, "inhouseDB", mock(InhouseDB.class));
        inject(signals, "inventoryManager", mock(InventoryManager.class));
        inject(signals, "materialsManager", mock(MaterialsManager.class));
        inject(signals, "sampleManager", mock(SampleManager.class));
        inject(signals, "experimentManager", mock(ExperimentManager.class));
        inject(signals, "ldapClient", mock(LdapClient.class));
        inject(signals, "logConfig", mock(LogConfig.class));
        inject(signals, "signalsEntityManager", mock(SignalsEntityManager.class));
        inject(signals, "elementManager", mock(ElementManager.class));

        elementConfig = mock(ElementConfig.class);
        inject(signals, "elementConfig", elementConfig);
    }

}
