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

package de.ipb_halle.inhouse;

import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.field.Field;
import de.ipb_halle.signals.field.FieldValue;
import de.ipb_halle.signals.materials.Material;
import de.ipb_halle.signals.materials.Synonym;
import de.ipb_halle.signals.users.IUser;
import de.ipb_halle.signals.users.UserReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class InhouseCompoundDTO {
    private Integer id;
    private String eid;
    private Integer molId;
    private String casrn;
    private String remarks;
    private String ipbCode;
    private String name;
    private Set<Synonym> synonyms;
    private InhouseDB inhouseDB;

    private final Logger logger = LogManager.getLogger(InhouseCompound.class);

    public InhouseCompoundDTO(InhouseCompound inhouseCompound) {
        this.id = inhouseCompound.getId();
        this.eid = inhouseCompound.getEid();
        this.molId = inhouseCompound.getMolId();
        this.casrn = inhouseCompound.getCasrn();
        this.remarks = inhouseCompound.getRemarks();
        this.name = inhouseCompound.getName();

        synonyms = new HashSet<>();
        synonyms = inhouseCompound.getSynonyms();

        // logger.trace("InCoDTO is created: compound = {}\n", this.toString());
    }

    public InhouseCompound createEntity() {
        return new InhouseCompound()
                .setId(id)
                .setEid(eid == null || eid.isEmpty() ? "not given yet" : eid)
                .setMolId(molId)
                .setCasrn(casrn)
                .setRemarks(remarks)
                .setIpbCode(ipbCode == null || ipbCode.isEmpty() ? "There is no IPB code" : ipbCode)
                .setName(name);
    }

    public Material createAsset() throws IOException {
        Material mat = new Material();

        mat.setId(name == null || name.isEmpty() ? "no name record was found" + System.currentTimeMillis() : name);
        mat.setName(name == null || name.isEmpty() ? "no name record was found" + System.currentTimeMillis() : name);

        IUser iUser = new UserReference(inhouseDB.getConfigString(Compounds.COMPOUNDS_OWNER));
        mat.setCreatedBy(iUser);
        mat.setEditedBy(iUser);

        Date date = new Date(System.currentTimeMillis());
        mat.setCreatedAt(date);
        mat.setEditedAt(date);

        mat.setDescription(remarks == null || remarks.isEmpty() ? "no remarks was present" + System.currentTimeMillis() : remarks);
        mat.setEntityType(EntityType.valueOf(Material.ENTITY_TYPE_ASSET));

        mat.setLibraryId("assetType:" + inhouseDB.getConfigString(Compounds.COMPOUNDS_LIBRARY_ID));
        mat.setOwner(new UserReference(inhouseDB.getConfigString(Compounds.COMPOUNDS_OWNER)));

        mat.addAllSynonyms(synonyms);

        // logger.trace("InCo_DTO-> createAsset(): material = {}\n", mat.toString());

        createAssetFields(mat);
        return mat;
    }

    public Material createBatch() {
        Material mat = new Material();

        mat.setId(name == null || name.isEmpty() ? "no name record was found" : name);
        mat.setName(name == null || name.isEmpty() ? "no name record was found" : name);

        IUser iUser = new UserReference(inhouseDB.getConfigString(Compounds.COMPOUNDS_OWNER));
        mat.setCreatedBy(iUser);
        mat.setEditedBy(iUser);

        Date date = new Date(System.currentTimeMillis());
        mat.setCreatedAt(date);
        mat.setEditedAt(date);

        mat.setDescription(remarks == null || remarks.isEmpty() ? "no remarks was present" : remarks);
        mat.setEntityType(EntityType.valueOf(Material.ENTITY_TYPE_BATCH));

        mat.setLibraryId("assetType:" + inhouseDB.getConfigString(Compounds.COMPOUNDS_LIBRARY_ID));
        mat.setOwner(iUser);

        createBatchFields(mat);
        return mat;
    }

    private void createAssetFields(Material mat) throws IOException {
        // Load all fields from Library (21) and map them according to their Ids
        Map<String, Field> fieldsOfLibrary = receiveAllFieldsFromLibrary(mat.getLibraryId());

        // Add Field CAS-RN
        if ((casrn != null) && (!casrn.isEmpty())) {
            FieldValue fvCasrn = new FieldValue();
            fvCasrn.setFieldId(inhouseDB.getConfigString(Compounds.COMPOUNDS_FIELD_CASRN));
            fvCasrn.setValue(casrn);
            fvCasrn.setField(fieldsOfLibrary.get(fvCasrn.getFieldId()));
            mat.addFieldValue(fvCasrn);
        }

        // Add Field IPB_Code
        if ((ipbCode != null) && (!ipbCode.isEmpty())) {
            FieldValue fvIpbCode = new FieldValue();
            fvIpbCode.setFieldId(inhouseDB.getConfigString(Compounds.COMPOUNDS_FIELD_IPBCODE));
            fvIpbCode.setValue(ipbCode);
            fvIpbCode.setField(fieldsOfLibrary.get(fvIpbCode.getFieldId()));
            mat.addFieldValue(fvIpbCode);
        }

        // Add Field Mol_Id
        FieldValue fvMolId = new FieldValue();
        fvMolId.setFieldId(inhouseDB.getConfigString(Compounds.COMPOUNDS_FIELD_MOLID));
        fvMolId.setValue(Integer.toString(molId));
        fvMolId.setField(fieldsOfLibrary.get(fvMolId.getFieldId()));
        mat.addFieldValue(fvMolId);

        // Add Field Access
        FieldValue fvAccess = new FieldValue();
        fvAccess.setFieldId(inhouseDB.getConfigString(Compounds.COMPOUNDS_FIELD_ACCESS));
        fvAccess.setValue("NWC");
        fvAccess.setField(fieldsOfLibrary.get(fvAccess.getFieldId()));
        mat.addFieldValue(fvAccess);

        // Add Field Compound Name
        FieldValue fvName = new FieldValue();
        fvName.setFieldId(inhouseDB.getConfigString(Compounds.COMPOUNDS_FIELD_ASSET_NAME));
        fvName.setValue(name);
        fvName.setField(fieldsOfLibrary.get(fvName.getFieldId()));
        mat.addFieldValue(fvName);

        // Add Field Chemical Structure
        FieldValue drawing = new FieldValue();
        String fieldIdChemStruc = inhouseDB.getConfigString(Compounds.COMPOUNDS_FIELD_CHEMICAL_DRAWING);
        drawing.setFieldId(fieldIdChemStruc);
        String fieldValueCdxml = Files.readString(Path.of(String.format(inhouseDB.getConfigString(Compounds.COMPOUNDS_CHEMICAL_DRAWING), molId)), StandardCharsets.UTF_8);
        drawing.setField(fieldsOfLibrary.get(drawing.getFieldId()));
        drawing.setValue(fieldValueCdxml);
        mat.addFieldValue(drawing);
    }

    private Map<String, Field> receiveAllFieldsFromLibrary(String libraryId) {
        Map<String, Object> cmap = new HashMap<>();

        // Prefix for library id should be added
        cmap.put(Field.DEFINING_ENTITY_ID, String.format(libraryId));

        // overall 21 fields for InhouseCompound
        List<Field> fields = inhouseDB.getFieldDbService().loadFields(cmap);

        Map<String, Field> fieldsByIdMap = new HashMap<>();
        for (Field field : fields) {
            fieldsByIdMap.put(field.getId(), field);
        }

        return fieldsByIdMap;
    }

    private void createBatchFields(Material mat) {

        Map<String, Field> fieldMap = receiveAllFieldsFromLibrary(mat.getLibraryId());

        FieldValue fvName = new FieldValue();
        fvName.setFieldId(inhouseDB.getConfigString(Compounds.COMPOUNDS_FIELD_BATCH_NAME));
        fvName.setField(fieldMap.get(fvName.getFieldId()));
        fvName.setValue(inhouseDB.getConfigString(Compounds.COMPOUNDS_BATCH_NAME));
        mat.addFieldValue(fvName);

        FieldValue fvAmount = new FieldValue();
        fvAmount.setFieldId(inhouseDB.getConfigString(Compounds.COMPOUNDS_FIELD_BATCH_AMOUNT));
        fvAmount.setField(fieldMap.get(fvAmount.getFieldId()));
        fvAmount.setValue("12");
        mat.addFieldValue(fvAmount);

        FieldValue fvPurity = new FieldValue();
        fvPurity.setFieldId(inhouseDB.getConfigString(Compounds.COMPOUNDS_FIELD_BATCH_PURITY));
        fvPurity.setField(fieldMap.get(fvPurity.getFieldId()));
        fvPurity.setValue("99.9");
        mat.addFieldValue(fvPurity);
    }

    public void addSynonyms(Collection<InhouseSynonym> synonyms) {
        for (InhouseSynonym ics : synonyms) {
            this.synonyms.add(new Synonym("", ics.getSynonym()));
        }
    }

    public Integer getId() {
        return id;
    }

    public InhouseCompoundDTO setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getEid() {
        return eid;
    }

    public InhouseCompoundDTO setEid(String eid) {
        this.eid = eid;
        return this;
    }

    public Integer getMolId() {
        return molId;
    }

    public InhouseCompoundDTO setMolId(Integer molId) {
        this.molId = molId;
        return this;
    }

    public String getCasrn() {
        return casrn;
    }

    public InhouseCompoundDTO setCasrn(String casrn) {
        this.casrn = casrn;
        return this;
    }

    public String getRemarks() {
        return remarks;
    }

    public InhouseCompoundDTO setRemarks(String remarks) {
        this.remarks = remarks;
        return this;
    }

    public String getIpbCode() {
        return ipbCode;
    }

    public InhouseCompoundDTO setIpbCode(String ipbCode) {
        this.ipbCode = ipbCode;
        return this;
    }

    public String getName() {
        return name;
    }

    public InhouseCompoundDTO setName(String name) {
        this.name = name;
        return this;
    }

    public Set<Synonym> getSynonyms() {
        return synonyms;
    }

    public InhouseCompoundDTO setSynonyms(Set<Synonym> synonyms) {
        this.synonyms = synonyms;
        return this;
    }

    public InhouseDB getInhouseDB() {
        return inhouseDB;
    }

    public InhouseCompoundDTO setInhouseDB(InhouseDB inhouseDB) {
        this.inhouseDB = inhouseDB;
        return this;
    }


    @Override
    public String toString() {
        return "InhouseCompoundDTO{" +
                "id=" + id +
                ", eid='" + eid + '\'' +
                ", molId=" + molId +
                ", casrn='" + casrn + '\'' +
                ", remarks='" + remarks + '\'' +
                ", ipbCode='" + ipbCode + '\'' +
                ", name='" + name + '\'' +
                ", synonyms=" + synonyms +
                ", inhouseDB=" + inhouseDB +
                '}';
    }
}
