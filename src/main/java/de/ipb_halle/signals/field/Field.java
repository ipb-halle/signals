package de.ipb_halle.signals.field;

import java.util.*;

public class Field {

    public final static String ATTR_ATTRIBUTE_LIST_EID = "attributeListEid";
    public final static String ATTR_ATTRIBUTE = "attribute";
    public final static String ATTR_CALCULATED = "calculated";
    public final static String ATTR_COLLECTION = "collection";  // could be the same as type=list?
    public final static String ATTR_DATA_TYPE = "dataType";
    public final static String ATTR_DEFAULT_UNIT = "defaultUnit";
    public final static String ATTR_DEFINED_BY = "definedBy";
    public final static String ATTR_DEFINITION = "definition";
    public final static String ATTR_FIELD_TYPE = "type";
    public final static String ATTR_HIDDEN = "hidden";
    public final static String ATTR_KEY = "key";
    public final static String ATTR_MEASURE_OPTIONS = "measureOptions";
    public final static String ATTR_MEASURES = "measures";
    //public final static String ATTR_MULTISELECT = "multiSelect";
    public final static String ATTR_OPTIONS = "options";
    // public final static String ATTR_READ_ONLY = "readOnly";
    public final static String ATTR_REQUIRED = "isRequired";
    public final static String ATTR_MANDATORY = "mandatory";
    public final static String ATTR_TITLE = "title";
    public final static String ATTR_USER_DEFINED = "isUserDefined";

    /* keys for query criteria */
    public final static String FIELD_TITLE = "title";
    public final static String ENTITY_ID = "entityId";
    public final static String DEFINING_ENTITY_ID = "definingEntityId";
    public final static String FIELD_ID = "fieldId";
    public final static String FIELD_DESIGNATION = "designation";

    /* globally defined field Ids for drawings, images and sequences */
    public final static String FIELD_ID_CHEMICAL_DRAWING = "27b17174-c8e4-4a98-93e5-37ef97327fbd";
    public final static String FIELD_ID_IMAGE = "3f1f2fbc-fdd1-4c60-b6f8-41f54937d2cd";
    public final static String FIELD_ID_SEQUENCE = "8bf2bdc6-b5e1-4e23-8622-40a0bc0c0d96";


    private String id;
    private String attributeListEid;
    private Boolean calculated;
    private String defaultUnit;
    private String definedBy;
    private String definingEntityId;
    private Boolean hidden;
    private String key;
    private Boolean multiSelect;
    private Boolean readOnly;
    private Boolean required;
    private String title;
    private Boolean userDefined;
    /* complex types */
    private FieldType fieldType;
    private Set<FieldMeasure> measures;
    private Set<FieldOption> options;
    private FieldDesignation designation;

    public Field() {
        designation = FieldDesignation.valueOf(FieldDesignation.DEFAULT); //defines if filed belongs to asset, batch or is default
        this.measures = new HashSet<>();
        this.options = new HashSet<> ();
    }

    public Field(FieldDefinition fieldDefinition, FieldType type, FieldDesignation designation) {
        this.id = fieldDefinition.getId();
        this.attributeListEid = fieldDefinition.getAttributeListEid();
        this.calculated = fieldDefinition.getCalculated();
        this.defaultUnit = fieldDefinition.getDefaultUnit();
        this.definedBy = fieldDefinition.getDefinedBy();
        this.definingEntityId = fieldDefinition.getDefiningEntityId();
        this.hidden = fieldDefinition.isHidden();
        this.key = fieldDefinition.getKey();
        this.multiSelect = fieldDefinition.isMultiSelect();
        this.readOnly = fieldDefinition.isReadOnly();
        this.required = fieldDefinition.isRequired();
        this.title = fieldDefinition.getTitle();
        this.userDefined = fieldDefinition.isUserDefined();

        /* complex types */
        this.designation = designation;
        this.fieldType = type;
        this.measures = new HashSet<>();
        this.options = new HashSet<> ();
    }

    public FieldDefinition createEntity() {
        FieldDefinition def = new FieldDefinition();
        def.setId(id);
        def.setAttributeListEid(attributeListEid);
        def.setCalculated(calculated);
        def.setDefaultUnit(defaultUnit);
        def.setDefinedBy(definedBy);
        def.setDefiningEntityId(definingEntityId);
        def.setFieldDesignation(designation.getId());
        def.setFieldType(fieldType.getId());
        def.setHidden(hidden);
        def.setKey(key);
        def.setMultiSelect(multiSelect);
        def.setReadOnly(readOnly);
        def.setRequired(required);
        def.setTitle(title);
        def.setUserDefined(userDefined);
        return def;
    }

    public Field addAllMeasures(Collection<FieldMeasure> measures) {
        this.measures.addAll(measures);
        return this;
    }

    public Field addAllOptions(Collection<FieldOption> options) {
        this.options.addAll(options);
        return this;
    }

    public void addMeasure(FieldMeasure m) {
        measures.add(m);
    }

    public void addOption(FieldOption o) {
        options.add(o);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAttributeListEid() {
        return attributeListEid;
    }

    public void setAttributeListEid(String attributeListEid) {
        this.attributeListEid = attributeListEid;
    }

    public Boolean getCalculated() {
        return calculated;
    }

    public void setCalculated(Boolean calculated) {
        this.calculated = calculated;
    }

    public String getDefaultUnit() {
        return defaultUnit;
    }

    public void setDefaultUnit(String defaultUnit) {
        this.defaultUnit = defaultUnit;
    }

    public String getDefinedBy() {
        return definedBy;
    }

    public void setDefinedBy(String definedBy) {
        this.definedBy = definedBy;
    }

    public String getDefiningEntityId() {
        return definingEntityId;
    }

    public void setDefiningEntityId(String definingEntityId) {
        this.definingEntityId = definingEntityId;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Boolean getMultiSelect() {
        return multiSelect;
    }

    public void setMultiSelect(Boolean multiSelect) {
        this.multiSelect = multiSelect;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getUserDefined() {
        return userDefined;
    }

    public void setUserDefined(Boolean userDefined) {
        this.userDefined = userDefined;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    public Set<FieldMeasure> getMeasures() {
        return measures;
    }

    public Field setMeasures(Set<FieldMeasure> measures) {
        this.measures = measures;
        return this;
    }

    public Set<FieldOption> getOptions() {
        return options;
    }

    public Field setOptions(Set<FieldOption> options) {
        this.options = options;
        return this;
    }

    public FieldDesignation getDesignation() {
        return designation;
    }

    public void setDesignation(FieldDesignation designation) {
        this.designation = designation;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Field field = (Field) object;
        return Objects.equals(id, field.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Field{" +
                "id='" + id + '\'' +
                ", attributeListEid='" + attributeListEid + '\'' +
                ", calculated=" + calculated +
                ", defaultUnit='" + defaultUnit + '\'' +
                ", definedBy='" + definedBy + '\'' +
                ", hidden=" + hidden +
                ", key='" + key + '\'' +
                ", multiSelect=" + multiSelect +
                ", readOnly=" + readOnly +
                ", required=" + required +
                ", title='" + title + '\'' +
                ", userDefined=" + userDefined +
                ", fieldType=" + fieldType +
                ", measures=" + measures +
                ", options=" + options +
                ", designation=" + designation +
                '}';
    }
}
