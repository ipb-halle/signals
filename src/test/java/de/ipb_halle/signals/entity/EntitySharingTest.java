/*
 * IPB Signals client
 * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.signals.entity;

import de.ipb_halle.signals.PostgresqlContainerExtension;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.users.Group;
import de.ipb_halle.signals.users.GroupDbService;
import de.ipb_halle.signals.users.User;
import de.ipb_halle.signals.users.UserDbService;
import de.ipb_halle.tda.DeploymentElement;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(PostgresqlContainerExtension.class)
public abstract class EntitySharingTest {

    @PersistenceContext
    private EntityManager em;

    @Inject
    @DeploymentElement
    public SignalsEntityDbService signalsEntityDbService;

    @Inject
    @DeploymentElement
    private UserDbService userDbService;

    @Inject
    @DeploymentElement
    private GroupDbService groupDbService;

    @Inject
    @DeploymentElement
    private DynEnumManager dynEnumManager;

    private EntityType entityType;

    private Group createGroup(String id) {
        Group group = new Group();
        group.setId("sharing_" + id);
        group.setName("sharing_NAME_" + id);
        group.setDescription("sharing_DESCRIPTION_" + id);
        group.setCreatedAt(new Date());
        group.setEditedAt(new Date());
        group.setSystem(true);
        groupDbService.save(group);
        return group;
    }

    private User createUser(Group[] groups, String id) {
        User user = new User();
        user.setId("sharing_" + id);
        user.setUserName("sharing_NAME_" + id);
        user.setEnabled(true);
        for (Group g : groups) {
            user.addSystemGroup(g);
        }
        userDbService.save(user);
        return user;
    }

    private SignalsEntityDTO createEntity(String id, User user) {
        SignalsEntityDTO dto = new SignalsEntityDTO();
        dto.setId("sharing_" + id);
        dto.setEid("sharing_" + id);
        dto.setCreatedAt(new Date());
        dto.setCreatedBy(user);
        dto.setEditedAt(new Date());
        dto.setEditedBy(user);
        dto.setOwner(user);
        dto.setName("sharing_NAME_" + id);
        dto.setType(entityType);
        signalsEntityDbService.save(dto);
        return dto;
    }

    private void createUserShare(SignalsEntityDTO e, User u, boolean r, boolean w, boolean a, boolean f) {
        UserShare us = new UserShare();
        us.setEntityId(e.getId());
        us.setUserId(u.getId());
        us.setCanRead(r);
        us.setCanWrite(w);
        us.setIsAdmin(a);
        us.setHasFullControl(f);
        signalsEntityDbService.save(us);
    }

    private void createGroupShare(SignalsEntityDTO e, Group g, boolean r, boolean w, boolean a, boolean f) {
        GroupShare gs = new GroupShare();
        gs.setEntityId(e.getId());
        gs.setGroupId(g.getId());
        gs.setCanRead(r);
        gs.setCanWrite(w);
        gs.setIsAdmin(a);
        gs.setHasFullControl(f);
        signalsEntityDbService.save(gs);
    }

    private List<Share> getShare(SignalsEntityDTO e, User u) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(Share.ENTITY_ID, e.getId());
        cmap.put(Share.USER_ID, u.getId());
        // this.em.flush();
        this.em.clear();
        return signalsEntityDbService.loadEffectiveShares(cmap);

    }

    @Test
    public void sharingTest() {
        dynEnumManager.allowEnumDiscovery();
        entityType = (EntityType) dynEnumManager.valueOf(EntityType.valueOf("sharing_experiment"));
        Group g1 = createGroup("g1");
        Group g2 = createGroup("g2");
        Group g3 = createGroup("g3");
        User u1 = createUser(new Group[] {g1, g2}, "u1");
        User u2 = createUser(new Group[] {g1}, "u2");
        User u3 = createUser(new Group[] {g3}, "u3");
        SignalsEntityDTO e1 = createEntity("e1", u1);
        SignalsEntityDTO e2 = createEntity("e2", u2);
        SignalsEntityDTO e3 = createEntity("e3", u2);
        createUserShare(e1, u1, true, false, false, false);
        List<Share> shareList = getShare(e1, u1);
        Assertions.assertEquals(1,shareList.size(), "List size matches");
        Assertions.assertTrue(shareList.get(0).canRead(), "canRead granted");
        Assertions.assertFalse(shareList.get(0).canWrite(), "canRead granted");
/*
        createGroupShare(e1, g1, true, false, false, false);
        createGroupShare(e1, g2, true, false, false, false);
        createGroupShare(e1, g3, true, false, false, false);
*/
    }
}
