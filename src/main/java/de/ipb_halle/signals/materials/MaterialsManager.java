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
package de.ipb_halle.signals.materials;

import de.ipb_halle.signals.RuntimeConfig;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.entity.SignalsEntityDbService;
import de.ipb_halle.signals.entity.SignalsEntityRestService;
import de.ipb_halle.signals.rest.RestHelper;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** 
 * Manager for signals materials libraries (materials/libraries API endpoint) 
 */

@Stateless
public class MaterialsManager {

    @Inject
    private LibraryDbService libraryDbService;

    @Inject
    private LibraryRestService libraryRestService;

    @Inject
    private MaterialDbService materialDbService;

    @Inject
    private MaterialRestService materialRestService;

    @Inject
    private SignalsEntityDbService signalsEntityDbService;

    private Logger logger = LoggerFactory.getLogger(MaterialsManager.class);


    public void manageLibraries(RuntimeConfig config) {
        fetchLibraries(config);
    }

    public void manageMaterials(RuntimeConfig config, Date[] dateRange) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(SignalsEntityRestService.PARAMETER_START, dateRange[0]);
        if (dateRange.length > 1) {
            cmap.put(SignalsEntityRestService.PARAMETER_END, dateRange[1]);
        }
        cmap.put(SignalsEntityRestService.PARAMETER_INCLUDE_TYPES, Material.ENTITY_TYPE_ASSET);
        List<SignalsEntityDTO> entityDTOs = signalsEntityDbService.load(cmap);
        for (SignalsEntityDTO dto : entityDTOs) {
            Material mat = materialRestService.doGetMaterial(dto.getId());
            materialDbService.save(mat);
        }
    }
    
    private void fetchLibraries(RuntimeConfig config) {
        List<Library> libraries = libraryRestService.doGetLibraries();
        for (Library lib : libraries) {
            if (config.updateDb) {
                libraryDbService.save(lib);
            }
        }
    }
}


