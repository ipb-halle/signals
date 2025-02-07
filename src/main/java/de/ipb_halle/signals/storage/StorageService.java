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
import de.ipb_halle.signals.attachment.AttachmentFile;
import de.ipb_halle.signals.rest.RestReply;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Stateless
public class StorageService {

    // 512 kiB maximum size for Base64 encoded data
    public final static int MAX_SIZE_BASE64 = 512 * 1024;

    @Resource
    private SignalsConfig signalsConfig;

    private final static Map<String, String> standardMimeExtensions = new HashMap<>();
    private final static Map<String, Integer> standardMimePreferences = new HashMap<>();

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

        standardMimePreferences.put("application/vnd.api+json", 1);
        standardMimePreferences.put("application/scim+json", 1);
        standardMimePreferences.put("application/octet-stream", 1);
        standardMimePreferences.put("image/svg+xml", 1);
        standardMimePreferences.put("chemical/x-cdxml", 10);
        standardMimePreferences.put("chemical/x-mdl-molfile-v3000", 5);
        standardMimePreferences.put("chemical/x-daylight-smiles", 1);
        standardMimePreferences.put("chemical/x-mdl-sdfile", 3);
        standardMimePreferences.put("biosequence/fasta", 1);
        standardMimePreferences.put("biosequence/genbank", 2);
        standardMimePreferences.put("text/csv", 1);
        standardMimePreferences.put("image/*", 1);
        standardMimePreferences.put("image/png", 1);
        standardMimePreferences.put("image/jpeg", 1);
        standardMimePreferences.put("image/tiff", 1);


    }

    private Logger logger = LoggerFactory.getLogger(StorageService.class);

    public void removeFromStaging(RestReply file) throws IOException {
        Files.delete(file.getPath());
    }

    public void storeFile(AttachmentFile file) throws IOException {
        Path destination = computePath(file);
        try {
            Files.createDirectories(destination.getParent());
            Files.move(file.getTempPath(), destination, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            this.logger.warn("storeFile() caught IOException on move {} -> {} ({})",
                    file.getTempPath().toFile(), destination.toString(), e.getMessage());
            throw(e);
        }
    }

    private Path computePath(AttachmentFile file) {
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

    /**
     * Obtain a base64 encoded version of an Attachment. Return the
     * preferred version, where CDXML is preferred over MDL MOL, which
     * is preferred over SMILES. In the same way, GenBank is preferred
     * over FASTA.
     * @param files
     * @return
     */
    public String getFileBase64(Set<AttachmentFile> files) {
        AttachmentFile preferred = selectPreferredMimeType(files);
        if (preferred.getSize() < MAX_SIZE_BASE64) {
            try {
                Path source = computePath(preferred);
                byte[] b = Files.readAllBytes(source);
                return Base64.getEncoder().encodeToString(b);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return "";
    }

    public AttachmentFile selectPreferredMimeType(Set<AttachmentFile> files) {
        AttachmentFile preferred = null;
        int level = 0;
        for(AttachmentFile file : files) {
            int l = standardMimePreferences.get(file.getMimeType());
            if (l > level) {
                preferred = file;
                level = l;
            }
        }
        return preferred;
    }
}
