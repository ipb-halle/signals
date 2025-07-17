package de.ipb_halle.signals.experiments.properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExperimentPropertyValue {

    private String experimentId;
    private String propertyId;
    private String propertyValue;
    private ExperimentProperty property;
    public static final Logger logger = LogManager.getLogger(ExperimentPropertyValue.class);

    public ExperimentPropertyValue() {
    }

    public ExperimentPropertyValue(ExperimentPropertyValueEntity entity) {
        this.experimentId = entity.getId().getExperimentId();
        this.propertyId = entity.getId().getPropertyId();
        this.propertyValue = entity.getPropertyValue();
    }

    public ExperimentPropertyValueEntity createEntity() {
        if (experimentId == null || propertyId == null) {
            logger.warn("ExperimentPropertyValue:-> Skipping SamplePropertyValueEntity creation");
            return null;
        }

        ExperimentPropertyValueId experimentPropertyValueId = new ExperimentPropertyValueId();
        experimentPropertyValueId
                .setExperimentId(experimentId)
                .setPropertyId(propertyId);

        return new ExperimentPropertyValueEntity()
                .setId(experimentPropertyValueId)
                .setPropertyValue(propertyValue);
    }

    // ——— getters ——— //

    public String getExperimentId() {
        return experimentId;
    }


    public String getPropertyId() {
        return propertyId;
    }


    public String getPropertyValue() {
        return propertyValue;
    }

    public ExperimentProperty getProperty() {
        return property;
    }

    // ——— Setters ——— //

    public ExperimentPropertyValue setExperimentId(String experimentId) {
        this.experimentId = experimentId;
        return this;
    }

    public ExperimentPropertyValue setPropertyId(String propertyId) {
        this.propertyId = propertyId;
        return this;
    }

    public ExperimentPropertyValue setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
        return this;
    }

    public ExperimentPropertyValue setProperty(ExperimentProperty property) {
        this.property = property;
        return this;
    }

    @Override
    public String toString() {
        return "ExperimentPropertyValue{" +
                "experimentId='" + experimentId + '\'' +
                ", propertyId='" + propertyId + '\'' +
                ", propertyValue='" + propertyValue + '\'' +
                '}';
    }
}
