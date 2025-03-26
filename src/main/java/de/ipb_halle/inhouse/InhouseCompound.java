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

package de.ipb_halle.inhouse;

import de.ipb_halle.signals.entity.EntityType;
import de.ipb_halle.signals.field.FieldValue;
import de.ipb_halle.signals.materials.Material;
import de.ipb_halle.signals.materials.Synonym;
import de.ipb_halle.signals.users.UserReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="inhouse_compounds")
public class InhouseCompound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String eid;

    @Column(name = "mol_id")
    private Integer molId;
    @Column
    private String casrn;
    @Column
    private String remarks;
    @Column(name = "ipb_code")
    private String ipbCode;
    @Column
    private String name;

    private transient Set<Synonym> synonyms;


    public InhouseCompound() {
        synonyms = new HashSet<>();
    }

    public Material createAsset(InhouseDB inhouseDB) throws IOException {
        Material mat = new Material();
        mat.setCreatedBy(new UserReference(inhouseDB.getConfigString(Compounds.COMPOUNDS_OWNER)));
        mat.setDescription(remarks);
        mat.setEntityType(EntityType.valueOf(Material.ENTITY_TYPE_ASSET));
        mat.setLibraryId(inhouseDB.getConfigString(Compounds.COMPOUNDS_LIBRARY_ID));
        mat.setOwner(new UserReference(inhouseDB.getConfigString(Compounds.COMPOUNDS_OWNER)));
        mat.addAllSynonyms(synonyms);
        createAssetFields(inhouseDB, mat);
        addChemicalDrawing(inhouseDB, mat);
        return mat;
    }

    public Material createBatch(InhouseDB inhouseDB) {
        Material mat = new Material();
        mat.setEntityType(EntityType.valueOf(Material.ENTITY_TYPE_BATCH));
        createBatchFields(inhouseDB, mat);
        return mat;
    }

    private void addChemicalDrawing(InhouseDB inhouseDB, Material mat) throws IOException {
        FieldValue drawing = new FieldValue();
        drawing.setFieldId(inhouseDB.getConfigString(Compounds.COMPOUNDS_FIELD_CHEMICAL_DRAWING));
        drawing.setValue(Files.readString(Path.of(
                String.format(inhouseDB.getConfigString(Compounds.COMPOUNDS_CHEMICAL_DRAWING), molId)
                ), StandardCharsets.UTF_8));
        mat.addFieldValue(drawing);
    }

    private void createAssetFields(InhouseDB inhouseDB, Material mat) {
        if ((casrn != null) && (! casrn.isEmpty())) {
            FieldValue fvCasrn = new FieldValue();
            fvCasrn.setFieldId(inhouseDB.getConfigString(Compounds.COMPOUNDS_FIELD_CASRN));
            fvCasrn.setValue(casrn);
            mat.addFieldValue(fvCasrn);
        }

        if ((ipbCode != null) && (! ipbCode.isEmpty())) {
            FieldValue fvIpbCode = new FieldValue();
            fvIpbCode.setFieldId(inhouseDB.getConfigString(Compounds.COMPOUNDS_FIELD_IPBCODE));
            fvIpbCode.setValue(ipbCode);
            mat.addFieldValue(fvIpbCode);
        }

        FieldValue fvMolId = new FieldValue();
        fvMolId.setFieldId(inhouseDB.getConfigString(Compounds.COMPOUNDS_FIELD_MOLID));
        fvMolId.setValue(Integer.toString(molId));
        mat.addFieldValue(fvMolId);

        FieldValue fvAccess = new FieldValue();
        fvAccess.setFieldId(inhouseDB.getConfigString(Compounds.COMPOUNDS_FIELD_ACCESS));
        fvAccess.setValue(inhouseDB.getConfigString(Compounds.COMPOUNDS_ACCESS));
        mat.addFieldValue(fvAccess);

        FieldValue fvName = new FieldValue();
        fvName.setFieldId(inhouseDB.getConfigString(Compounds.COMPOUNDS_FIELD_ASSET_NAME));
        fvName.setValue(name);
        mat.addFieldValue(fvName);
    }

    private void createBatchFields(InhouseDB inhouseDB, Material mat) {
        FieldValue fvName = new FieldValue();
        fvName.setFieldId(inhouseDB.getConfigString(Compounds.COMPOUNDS_FIELD_BATCH_NAME));
        fvName.setValue(inhouseDB.getConfigString(Compounds.COMPOUNDS_BATCH_NAME));
        mat.addFieldValue(fvName);
    }

    public void addSynonyms(Collection<InhouseCompoundSynonym> synonyms) {
        for(InhouseCompoundSynonym ics : synonyms) {
            this.synonyms.add(new Synonym("", ics.getSynonym()));
        }
    }

    public Set<Synonym> getSynonyms() {
        return synonyms;
    }

    public String getEid() {
        return eid;
    }

    public InhouseCompound setEid(String eid) {
        this.eid = eid;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMolId() {
        return molId;
    }

    public InhouseCompound setMolId(Integer molId) {
        this.molId = molId;
        return this;
    }

    public String getCasrn() {
        return casrn;
    }

    public InhouseCompound setCasrn(String casrn) {
        this.casrn = casrn;
        return this;
    }

    public String getRemarks() {
        return remarks;
    }

    public InhouseCompound setRemarks(String remarks) {
        this.remarks = remarks;
        return this;
    }

    public String getIpbCode() {
        return ipbCode;
    }

    public InhouseCompound setIpbCode(String ipbCode) {
        this.ipbCode = ipbCode;
        return this;
    }

    public String getName() {
        return name;
    }

    public InhouseCompound setName(String name) {
        this.name = name;
        return this;
    }
}
