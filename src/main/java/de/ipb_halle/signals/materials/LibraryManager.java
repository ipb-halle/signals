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

import de.ipb_halle.signals.UpdateConfig;

import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** 
 * Manager for signals materials libraries (materials/libraries API endpoint) 
 */

@Stateless
public class LibraryManager {

    @Inject
    private LibraryDbService dbService;

    @Inject
    private LibraryRestService restService;

    private Logger logger = LoggerFactory.getLogger(LibraryManager.class);

    
    public Library getDbLibrary(String id) {
        return dbService.loadById(id);
    }

    public List<Library> getSnbLibraries() {
        return restService.doGetLibraries();
    }

    public void save(UpdateConfig config, List<Library> libraries) {
        for (Library lib : libraries) {
            logger.debug("materials library save({})", lib.getAssetDisplayName());
            if (config.updateDb) {
                dbService.save(lib);
            }
        }
    }


    public void manageMaterials(UpdateConfig config) {
        syncLibraries(config);
    }

    private void syncLibraries(UpdateConfig config) {
        List<Library> libraries = getSnbLibraries();
        save(config, libraries);
    }
}


