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
import de.ipb_halle.signals.TestHelper;
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

    private TestHelper testHelper;

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
        return signalsEntityDbService.loadEffectiveShares(cmap);
    }

    private void setup() {
        dynEnumManager.allowEnumDiscovery();
        testHelper = new TestHelper()
                .setDynEnumManager(dynEnumManager)
                .setGroupDbService(groupDbService)
                .setSignalsEntityDbService(signalsEntityDbService)
                .setUserDbService(userDbService)
                .setPrefix("sharing_");
    }

    @Test
    public void sharingTest() {
        setup();
        EntityType entityType = EntityType.valueOf("sharing_experiment");
        Group g1 = testHelper.createGroup("g1");
        Group g2 = testHelper.createGroup("g2");
        Group g3 = testHelper.createGroup("g3");
        User u1 = testHelper.createUser("u1", new Group[] {g1, g2});
        User u2 = testHelper.createUser("u2", new Group[] {g1});
        User u3 = testHelper.createUser("u3", new Group[] {g3});
        SignalsEntityDTO e1 = testHelper.createEntity("e1", entityType, u1);
        SignalsEntityDTO e2 = testHelper.createEntity("e2", entityType, u2);
        SignalsEntityDTO e3 = testHelper.createEntity("e3", entityType, u2);
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
