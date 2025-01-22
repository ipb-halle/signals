package de.ipb_halle.signals.field;

import de.ipb_halle.signals.dynEnum.DynEnum;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("FieldDesignation")
public class FieldDesignation extends DynEnum<FieldDesignation> {

    public final static String DEFAULT = "default";
    public final static String ASSET = "asset";
    public final static String BATCH = "batch";
    public final static String CONTAINER = "container";
    public final static String LOCATION = "location";
    public final static String SAMPLE = "sample";
    public final static String EXPERIMENT = "experiment";
    public final static String NOTEBOOK = "notebook";

    public FieldDesignation() {
    }

    public FieldDesignation(String v) {
        super(v);
    }

    public static FieldDesignation valueOf(String v) {
        return new FieldDesignation(v);
    }
}
