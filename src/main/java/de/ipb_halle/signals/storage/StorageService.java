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

package de.ipb_halle.signals.storage;

import de.ipb_halle.signals.SignalsConfig;
import de.ipb_halle.signals.attachment.Attachment;
import de.ipb_halle.signals.attachment.AttachmentFile;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;

@Stateless
public class StorageService {

    @Resource
    private SignalsConfig signalsConfig;

    private final static Map<String, String> standardMimeExtensions = new HashMap<>();

    static {
        standardMimeExtensions.put("application/vnd.api+json", "json");
        standardMimeExtensions.put("application/scim+json", "json");
        standardMimeExtensions.put("application/octet-stream", "bin");
        standardMimeExtensions.put("image/svg+xml", "svg");
        standardMimeExtensions.put("chemical/x-cdxml", "cdxml");
        standardMimeExtensions.put("chemical/x-mdl-molfile-v3000", "mol");
        standardMimeExtensions.put("chemical/x-daylight-smiles", "smiles");
        standardMimeExtensions.put("chemical/x-mdl-sdfile", "sdf");
        standardMimeExtensions.put("biosequence/fasta", "fasta");
        standardMimeExtensions.put("biosequence/genbank", "gb");
        standardMimeExtensions.put("text/csv", "csv");
        standardMimeExtensions.put("image/*", "image");
        standardMimeExtensions.put("image/png", "png");
        standardMimeExtensions.put("image/jpeg", "jpeg");
        standardMimeExtensions.put("image/tiff", "tiff");
    }

    private Logger logger = LoggerFactory.getLogger(StorageService.class);

    public void storeFile(AttachmentFile file) throws IOException {
        Path destination = computeDestination(file);
        try {
            Files.createDirectories(destination.getParent());
            Files.move(file.getTempPath(), destination, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            this.logger.warn("storeFile() caught IOException on move {} -> {} ({})",
                    file.getTempPath().toFile(), destination.toString(), e.getMessage());
            throw(e);
        }
    }

    private Path computeDestination(AttachmentFile file) {
        int id = file.getId();
        String a = String.format("%02d", (id / 1_000_000) % 100);
        String b = String.format("%02d", (id / 10_000) % 100);
        String c = String.format("%02d", (id / 100) % 100);
        String name = String.format("%d.%d.%s",
                file.getId(),
                file.getRevisionId(),
                standardMimeExtensions.getOrDefault(file.getMimeType(), "dat"));
        return Paths.get(signalsConfig.getStoragePath(), a, b, c, name);
    }
}
