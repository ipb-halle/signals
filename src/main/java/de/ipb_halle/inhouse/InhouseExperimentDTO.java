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
import de.ipb_halle.signals.entity.SignalsEntity;
import de.ipb_halle.signals.experiments.Experiment;
import de.ipb_halle.signals.experiments.properties.ExperimentProperty;
import de.ipb_halle.signals.experiments.properties.ExperimentPropertyEntity;
import de.ipb_halle.signals.experiments.properties.ExperimentPropertyValue;
import de.ipb_halle.signals.users.IUser;
import de.ipb_halle.signals.users.UserReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO (Data Transfer Object) class for representing an experiment from the Inhouse database.
 * It serves as a converter between the old InhouseExperiment format and the Signals Experiment entity.
 * Provides methods to create both Inhouse and Signals experiments based on its fields.
 */
public class InhouseExperimentDTO {

    public static final String TEMPLATE_ID_INHOUSE_EXPERIMENT = "experiment:834e6aee-0d59-4732-89d7-925edca09844";
    public static final String JOURNAL_EID_WSE_AS_SAMPLE = "journal.templateId";
    private final Logger logger = LogManager.getLogger(InhouseExperimentDTO.class);

    private Integer id;
    private String eid;
    private String threelc;
    private String individualCode;
    private String journal;
    private int procId;
    private String remarks;
    private boolean importSuccessful;
    private String ipbCode;

    private InhouseDB inhouseDB;

    /**
     * Constructs an InhouseExperimentDTO from an existing InhouseExperiment entity.
     *
     * @param inhouseExperiment the source experiment from the old Inhouse database
     */
    public InhouseExperimentDTO(InhouseExperiment inhouseExperiment) {
        this.id = inhouseExperiment.getId();
        this.eid = inhouseExperiment.getEid();
        this.threelc = inhouseExperiment.getThreelc();
        this.individualCode = inhouseExperiment.getIndividualCode();
        this.journal = inhouseExperiment.getJournal();
        this.procId = inhouseExperiment.getProcId();
        this.remarks = inhouseExperiment.getRemarks();
        this.importSuccessful = inhouseExperiment.isImportSuccessful();
    }

    /**
     * Reconstructs an InhouseExperiment entity from the DTO fields.
     *
     * @return a new InhouseExperiment object
     */
    public InhouseExperiment createEntity() {
        return new InhouseExperiment()
                .setId(id)
                .setEid(eid)
                .setThreelc(threelc)
                .setIndividualCode(individualCode)
                .setJournal(journal)
                .setProcId(procId)
                .setRemarks(remarks)
                .setImportSuccessful(importSuccessful);
    }

    /**
     * Creates a new Signals Experiment object based on the DTO data.
     * Assigns metadata such as template ID, user reference, timestamps, and field values.
     *
     * @return a populated Signals Experiment object
     */
    public Experiment createExperiment(String experimentName) {
        //logger.info("I AM IN InhouseExperimentDTO in method  createExperiment()\n");

        Experiment experiment = new Experiment();

        experiment.setId(id == null ? "Currently not created" + System.currentTimeMillis() : id.toString());
        experiment.setName(eid == null ? String.format("Experiment of %s ", experimentName)  : eid);

        IUser iUser = new UserReference(threelc);
        experiment.setCreatedBy(iUser);
        experiment.setEditedBy(iUser);

        Date date = new Date(System.currentTimeMillis());
        experiment.setCreatedAt(date);
        experiment.setEditedAt(date);

        //experiment.setDescription(remarks == null || remarks.isEmpty() ? "no remarks was present" : remarks);
        experiment.setType(EntityType.valueOf(Experiment.ENTITY_TYPE_EXPERIMENT));

        experiment.setTemplateId(TEMPLATE_ID_INHOUSE_EXPERIMENT);
        experiment.setOwner(iUser);

        //toDo its necessary first to create a journal entity, otherwise use hardcoded ID!!
        SignalsEntity journal = new SignalsEntity();
        journal.setEid(inhouseDB.getConfigString(JOURNAL_EID_WSE_AS_SAMPLE));
        experiment.addAncestor(journal);
        experiment.setAncestorId(inhouseDB.getConfigString(JOURNAL_EID_WSE_AS_SAMPLE));


        createExperimentProperties(experiment);
        return experiment;
    }

    /**
     * Populates the given Signals Experiment with specific property values.
     * The values are taken from the DTO and matched to property IDs defined in the configuration.
     *
     * @param experiment the Signals Experiment to which properties are added
     */
    private void createExperimentProperties(Experiment experiment) {
        // 1) Receive a property id in dependence of Experiment Property as a Hash Map
        Map<String, ExperimentProperty> fieldsOfExperimentTemplate = receiveAllFieldsFromTemplateId(experiment.getTemplateId());

        // 2) Add Field Three_Letter_Code
        ExperimentPropertyValue fvThreeLc = new ExperimentPropertyValue();
        fvThreeLc.setPropertyId(inhouseDB.getConfigString(Experiments.EXPERIMENTS_FIELD_THREELC));
        fvThreeLc.setPropertyValue(threelc);
        fvThreeLc.setProperty(fieldsOfExperimentTemplate.get(fvThreeLc.getPropertyId()));
        experiment.addPropertyValue(fvThreeLc);

//        // 3) Add Field Individual_code
//        ExperimentPropertyValue fvIndividualCode = new ExperimentPropertyValue();
//        fvIndividualCode.setPropertyId(inhouseDB.getConfigString(Experiments.EXPERIMENTS_FIELD_INDIVIDUAL_CODE));
//        fvIndividualCode.setPropertyValue(individualCode);
//        fvIndividualCode.setExperimentProperty(fieldsOfExperimentTemplate.get(fvIndividualCode.getPropertyId()));
//        experiment.addPropertyValue(fvIndividualCode);
//
//        // 4) Add Field Journal
//        ExperimentPropertyValue fvJournal = new ExperimentPropertyValue();
//        fvJournal.setPropertyId(inhouseDB.getConfigString(Experiments.EXPERIMENTS_FIELD_JOURNAL));
//        fvJournal.setPropertyValue(journal);
//        fvJournal.setExperimentProperty(fieldsOfExperimentTemplate.get(fvJournal.getPropertyId()));
//        experiment.addPropertyValue(fvJournal);
//
//        // 5) Add Field Procedure_id
//        ExperimentPropertyValue fvProcId = new ExperimentPropertyValue();
//        fvProcId.setPropertyId(inhouseDB.getConfigString(Experiments.EXPERIMENTS_FIELD_PROCEDURE_ID));
//        fvProcId.setPropertyValue(String.valueOf(procId));
//        fvProcId.setExperimentProperty(fieldsOfExperimentTemplate.get(fvProcId.getPropertyId()));
//        experiment.addPropertyValue(fvProcId);
    }

    /**
     * Loads all experiment properties (fields) for a given template ID from the Signals database,
     * using the InhouseDB’s ExperimentDbService.
     *
     * @param templateId the template ID of the experiment
     * @return a map of property ID to ExperimentProperty
     */
    private Map<String, ExperimentProperty> receiveAllFieldsFromTemplateId(String templateId) {
        // 1) Criteria HashMap for Querying Experiment Entities
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(ExperimentProperty.TEMPLATE_ID, templateId);

        // 2) Loading of Experiment Property Entities from Database with the help of Experiment template id
        List<ExperimentPropertyEntity> fields = inhouseDB.getExperimentDbService().loadExperimentPropertyValues(cmap);

        // 3) Generation of property HashMap
        Map<String, ExperimentProperty> propertyMap = new HashMap<>();

        // 4) Transformation of Experiment Property Entities to Experiment Property POJO and putting in resulting HashMap
        for (ExperimentPropertyEntity entity : fields) {
            ExperimentProperty experimentProperty = new ExperimentProperty(entity);
            propertyMap.put(experimentProperty.getPropertyId(), experimentProperty);
        }
        return propertyMap;
    }


    // Setters AND Getters
    public Integer getId() {
        return id;
    }

    public InhouseExperimentDTO setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getEid() {
        return eid;
    }

    public InhouseExperimentDTO setEid(String eid) {
        this.eid = eid;
        return this;
    }

    public String getThreelc() {
        return threelc;
    }

    public InhouseExperimentDTO setThreelc(String threelc) {
        this.threelc = threelc;
        return this;
    }

    public String getIndividualCode() {
        return individualCode;
    }

    public InhouseExperimentDTO setIndividualCode(String individualCode) {
        this.individualCode = individualCode;
        return this;
    }

    public String getJournal() {
        return journal;
    }

    public InhouseExperimentDTO setJournal(String journal) {
        this.journal = journal;
        return this;
    }

    public int getProcId() {
        return procId;
    }

    public InhouseExperimentDTO setProcId(int procId) {
        this.procId = procId;
        return this;
    }

    public String getRemarks() {
        return remarks;
    }

    public InhouseExperimentDTO setRemarks(String remarks) {
        this.remarks = remarks;
        return this;
    }

    public InhouseDB getInhouseDB() {
        return inhouseDB;
    }

    public InhouseExperimentDTO setInhouseDB(InhouseDB inhouseDB) {
        this.inhouseDB = inhouseDB;
        return this;
    }

    public boolean isImportSuccessful() {
        return importSuccessful;
    }

    public InhouseExperimentDTO setImportSuccessful(boolean importSuccessful) {
        this.importSuccessful = importSuccessful;
        return this;
    }

    public String getIpbCode() {
        return ipbCode;
    }

    public InhouseExperimentDTO setIpbCode(String ipbCode) {
        this.ipbCode = ipbCode;
        return this;
    }
}
