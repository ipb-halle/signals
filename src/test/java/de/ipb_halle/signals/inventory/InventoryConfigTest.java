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
import de.ipb_halle.signals.Signals;
import de.ipb_halle.signals.SignalsConfig;
import org.apache.commons.cli.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.mockito.Mockito.*;

public class InventoryConfigTest {

    private InventoryConfig inventoryConfig;
    private InventoryManager inventoryManager;
    private SignalsConfig signalsConfig;
    private RuntimeConfig runtimeConfig;

    @BeforeEach
    public void setUp() {
        inventoryManager = mock(InventoryManager.class);
        signalsConfig = mock(SignalsConfig.class);
        runtimeConfig = new RuntimeConfig();
        inventoryConfig = new InventoryConfig(signalsConfig, runtimeConfig, inventoryManager);

        when(signalsConfig.getSnbInstanceName()).thenReturn("TEST_INSTANCE");

    }

    @Test
    public void testManagerInventory_shouldDelegate() {
        Date[] dateRange = new Date[]{new Date()};

        inventoryConfig.manageInventory(dateRange);

        verify(inventoryManager).manageInventory(dateRange);
    }

    @Test
    public void testImportLocations_shouldDelegate() {
        inventoryConfig.importLocations("loc:123");

        verify(inventoryManager).importLocation(runtimeConfig, "loc:123");
    }

    @Test
    public void testImportContainers_shouldDelegate() {
        inventoryConfig.importContainers("cont:123");

        verify(inventoryManager).importContainer(runtimeConfig, "cont:123");
    }

    @Test
    public void testProcessCommandLine_shouldTriggerManageInventory() throws Exception {
        Date[] dateRange = new Date[]{new Date()};

        Signals signals = mock(Signals.class);
        when(signalsConfig.getSnbInstanceName()).thenReturn("TEST");

        when(signals.getInventoryConfig()).thenReturn(inventoryConfig);

        Options options = new Options();
        InventoryConfig.registerOptions(options);
        CommandLineParser parser = new DefaultParser();

        CommandLine cmd = parser.parse(options, new String[]{"-iS", "2024-01-01"});

        InventoryConfig.processCommandLine(cmd, options, signals);

        inventoryConfig.manageInventory(dateRange);

        verify(inventoryManager).manageInventory(dateRange);
    }

    @Test
    public void testProcessCommandLine_shouldTriggerImportLocation() throws Exception {
        Signals signals = mock(Signals.class);
        InventoryConfig config = mock(InventoryConfig.class);
        when(signals.getInventoryConfig()).thenReturn(config);

        Options options = new Options();
        InventoryConfig.registerOptions(options);
        CommandLineParser parser = new DefaultParser();

        CommandLine cmd = parser.parse(options, new String[]{"-li", "loc:001"});

        InventoryConfig.processCommandLine(cmd, options, signals);

        verify(config).importLocations("loc:001");
    }

    @Test
    public void testProcessCommandLine_shouldTriggerImportContainer() throws Exception {
        Signals signals = mock(Signals.class);
        InventoryConfig config = mock(InventoryConfig.class);
        when(signals.getInventoryConfig()).thenReturn(config);

        Options options = new Options();
        InventoryConfig.registerOptions(options);
        CommandLineParser parser = new DefaultParser();

        CommandLine cmd = parser.parse(options, new String[]{"-ci", "cont:001"});

        InventoryConfig.processCommandLine(cmd, options, signals);

        verify(config).importContainers("cont:001");
    }
}
