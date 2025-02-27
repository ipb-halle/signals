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

package de.ipb_halle.signals.attachment;

import de.ipb_halle.signals.PostgresqlContainerExtension;
import de.ipb_halle.signals.TestHelper;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.entity.SignalsEntityDTO;
import de.ipb_halle.signals.entity.SignalsEntityDbService;
import de.ipb_halle.signals.rest.RestClient;
import de.ipb_halle.signals.users.Group;
import de.ipb_halle.signals.users.GroupDbService;
import de.ipb_halle.signals.users.User;
import de.ipb_halle.signals.users.UserDbService;
import de.ipb_halle.tda.DeploymentElement;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AssertionsKt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(PostgresqlContainerExtension.class)
public abstract class AttachmentDbServiceTest {

    private final static String TEST_PREFIX = "attchmntDbSvc_";
    private final static String TEST_DIGEST_1 = "some made-up value 1";
    private final static String TEST_DIGEST_2 = "some made-up value 2";
    private final static String TEST_FILE_NAME = "test.txt";
    private final static long TEST_FILE_SIZE_1 = 1234;
    private final static long TEST_FILE_SIZE_2 = 5678;

    @Inject
    @DeploymentElement
    private AttachmentDbService attachmentDbService;

    @Inject
    @DeploymentElement
    private DynEnumManager dynEnumManager;

    @Inject
    @DeploymentElement
    private GroupDbService groupDbService;

    @Inject
    @DeploymentElement
    private SignalsEntityDbService signalsEntityDbService;

    @Inject
    @DeploymentElement
    private UserDbService userDbService;

    private TestHelper testHelper;

    private void createAttachment(SignalsEntityDTO e, String digest) {
        Attachment attachment = new Attachment();
        attachment.setAncestorId(e.getId());
        AttachmentRevision rev = new AttachmentRevision();
        rev.setMimeType(RestClient.TEXT_PLAIN);
        rev.setOriginalName(TEST_FILE_NAME);
        rev.setSize(TEST_FILE_SIZE_1);
        attachment.addRevision(rev);
        AttachmentFile file = new AttachmentFile();
        file.setMimeType(RestClient.TEXT_PLAIN);
        file.setSize(TEST_FILE_SIZE_1);
        file.setDigest(digest);
        attachment.addFile(file);
        attachmentDbService.save(attachment);
    }

    private void createRevision(Attachment attachment) {
        AttachmentRevision rev = new AttachmentRevision();
        rev.setSize(TEST_FILE_SIZE_2);
        rev.setMimeType(RestClient.TEXT_PLAIN);
        rev.setOriginalName(TEST_FILE_NAME);
        attachment.addRevision(rev);
        AttachmentFile file = new AttachmentFile();
        file.setSize(TEST_FILE_SIZE_2);
        file.setMimeType(RestClient.TEXT_PLAIN);
        file.setDigest(TEST_DIGEST_2);
        attachment.addFile(file);
        attachmentDbService.save(attachment);
    }

    private void setup() {
        dynEnumManager.allowEnumDiscovery();
        testHelper = new TestHelper()
                .setDynEnumManager(dynEnumManager)
                .setGroupDbService(groupDbService)
                .setSignalsEntityDbService(signalsEntityDbService)
                .setUserDbService(userDbService)
                .setPrefix(TEST_PREFIX);
    }

    @Test
    void loadTest() {
        setup();
        User u1 = testHelper.createUser("u1", new Group[] {});
        SignalsEntityDTO e1 = testHelper.createEntity("e1", EntityType.valueOf("experiment"), u1);
        SignalsEntityDTO e2 = testHelper.createEntity("e2", EntityType.valueOf("experiment"), u1);
        createAttachment(e1, TEST_DIGEST_1);
        createAttachment(e2, TEST_DIGEST_2);
        Map<String, Object> cmap = new HashMap<>();
        List<Attachment> attachments = attachmentDbService.load(cmap);
        Assertions.assertEquals(2, attachments.size(), "number of attachments returned");

        cmap.put(Attachment.ANCESTOR_ID, e1.getId());
        attachments = attachmentDbService.load(cmap);
        Assertions.assertEquals(1, attachments.size(), "number of attachments returned");

        Attachment attachment = attachments.get(0);
        Assertions.assertEquals(1, attachment.getRevisions().size(), "number of revisions");
        Set<AttachmentFile> files = attachment.getFiles(attachment.getLatestRevision().getId());
        Assertions.assertEquals(1, files.size(), "Number of revision files" );
        AttachmentFile file = files.iterator().next();
        Assertions.assertEquals(TEST_FILE_SIZE_1, file.getSize(), "Size matches");
        Assertions.assertEquals(TEST_DIGEST_1, file.getDigest(), "digest matches");

        createRevision(attachment);
        attachments = attachmentDbService.load(cmap);
        attachment = attachments.get(0);
        Assertions.assertEquals(2, attachment.getRevisions().size(), "number of revisions matches");

        file = attachment.getFiles(
                attachment.getLatestRevision().getId())
                .iterator().next();
        Assertions.assertEquals(TEST_FILE_SIZE_2, file.getSize(), "Size matches");
        Assertions.assertEquals(TEST_DIGEST_2, file.getDigest(), "digest matches");
    }
}