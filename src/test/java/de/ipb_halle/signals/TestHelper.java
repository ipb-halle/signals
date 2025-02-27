/*
 *
 * IPB Signals client
 * Copyright 2025 Leibniz-Institut f. Pflanzenbiochemie
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

package de.ipb_halle.signals;

import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.entity.SignalsEntityDbService;
import de.ipb_halle.signals.users.Group;
import de.ipb_halle.signals.users.GroupDbService;
import de.ipb_halle.signals.users.User;
import de.ipb_halle.signals.users.UserDbService;

import java.util.Date;

public class TestHelper {

    private String prefix;

    private DynEnumManager dynEnumManager;
    private GroupDbService groupDbService;
    private SignalsEntityDbService signalsEntityDbService;
    private UserDbService userDbService;

    public TestHelper setDynEnumManager(DynEnumManager dynEnumManager) {
        this.dynEnumManager = dynEnumManager;
        return this;
    }

    public TestHelper setGroupDbService(GroupDbService groupDbService) {
        this.groupDbService = groupDbService;
        return this;
    }

    public TestHelper setSignalsEntityDbService(SignalsEntityDbService signalsEntityDbService) {
        this.signalsEntityDbService = signalsEntityDbService;
        return this;
    }

    public TestHelper setUserDbService(UserDbService userDbService) {
        this.userDbService = userDbService;
        return this;
    }

    public Group createGroup(String id) {
        Group group = new Group();
        group.setId(prefix + id);
        group.setName(prefix + "NAME_" + id);
        group.setDescription(prefix + "DESCRIPTION_" + id);
        group.setCreatedAt(new Date());
        group.setEditedAt(new Date());
        group.setSystem(true);
        groupDbService.save(group);
        return group;
    }

    public User createUser(String id, Group[] groups) {
        User user = new User();
        user.setId(prefix + id);
        user.setUserName(prefix + "NAME_" + id);
        user.setEnabled(true);
        for (Group g : groups) {
            user.addSystemGroup(g);
        }
        userDbService.save(user);
        return user;
    }

    public SignalsEntityDTO createEntity(String id, EntityType entityType, User user) {
        SignalsEntityDTO dto = new SignalsEntityDTO();
        dto.setId(prefix + id);
        dto.setEid(prefix + id);
        dto.setCreatedAt(new Date());
        dto.setCreatedBy(user);
        dto.setEditedAt(new Date());
        dto.setEditedBy(user);
        dto.setOwner(user);
        dto.setName(prefix + "NAME_" + id);
        dto.setType((EntityType) dynEnumManager.valueOf(entityType));
        signalsEntityDbService.save(dto);
        return dto;
    }

    public TestHelper setPrefix(String p) {
        prefix = p;
        return this;
    }
}
