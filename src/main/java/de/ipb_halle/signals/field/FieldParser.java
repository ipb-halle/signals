/*
 * IPB Signals client
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.signals.field;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.entity.MeasureMapper;
import de.ipb_halle.signals.entity.Quality;
import de.ipb_halle.signals.rest.RestHelper;
import de.ipb_halle.signals.rest.RestReplyParser;
import jakarta.ejb.Local;
import jakarta.inject.Inject;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import org.slf4j.Logger;

/**
 * service for field definitions (not a real REST service)
 */

@Local
public class FieldParser implements RestReplyParser<Field> {

    private Logger logger = LoggerFactory.getLogger(FieldParser.class);

    @Inject
    private DynEnumManager dynEnumMgr;

    /**
     * converts a field type from JSON to the respective
     * database backed field type class instance.
     *
     * @throws RuntimeException if field type is not yet registered and
     *                          auto discovery is not allowed (default).
     */
    private FieldType lookupFieldType(String typeString) {
        String norm = (typeString == null) ? "" : typeString.trim().toLowerCase();

        switch (norm) {
            case "text":
                return (FieldType) dynEnumMgr.valueOf(FieldType.text);
            case "unit":
                return (FieldType) dynEnumMgr.valueOf(FieldType.unit);
            case "attributelist":
                return (FieldType) dynEnumMgr.valueOf(FieldType.attributeList);
            case "multiselect":
                return (FieldType) dynEnumMgr.valueOf(FieldType.multiSelect);
            case "datetime":
                return (FieldType) dynEnumMgr.valueOf(FieldType.datetime);
            case "user":
                return (FieldType) dynEnumMgr.valueOf(FieldType.user);
            case "integer":
                // INTEGER (id=38)
                return (FieldType) dynEnumMgr.valueOf(FieldType.valueOf("INTEGER"));
            case "boolean":
                // 'BOOLEAN' (id=37)
                return (FieldType) dynEnumMgr.valueOf(FieldType.valueOf("BOOLEAN"));
            case "link":
                return (FieldType) dynEnumMgr.valueOf(FieldType.valueOf("LINK"));
            case "list":
                // 'list' (id=184)
                return (FieldType) dynEnumMgr.valueOf(FieldType.list);
            case "sequence_file":
                return (FieldType) dynEnumMgr.valueOf(FieldType.valueOf("SEQUENCE_FILE"));
            default:
                try {
                    return (FieldType) dynEnumMgr.valueOf(FieldType.valueOf(typeString));
                } catch (RuntimeException ex) {
                    throw new IllegalArgumentException("Unsupported FieldType: '" + typeString + "'", ex);
                }
        }
    }


    /**
     * ToDO: ATTR_COLLECTION currently not implemented!
     */
    @Override
    public Field parseReply(JsonElement json) {
        Field fd = new Field();
        JsonObject j = json.getAsJsonObject();
        fd.setId(j.getAsJsonPrimitive(RestHelper.ATTR_ID).getAsString());

        if (j.has(Field.ATTR_DEFINITION)) {
            /*
             * containers, ...
             */
            parseDefinition(j.getAsJsonObject(Field.ATTR_DEFINITION), fd);
        } else {
            /*
             *
             */
            fd.setAttributeListEid(RestHelper.parseString(j, Field.ATTR_ATTRIBUTE));
            fd.setCalculated(RestHelper.parseBool(j, Field.ATTR_CALCULATED, false));
            fd.setDefinedBy(RestHelper.parseString(j, Field.ATTR_DEFINED_BY));
            fd.setFieldType(lookupFieldType(RestHelper.parseString(j, Field.ATTR_DATA_TYPE)));
            fd.setHidden(RestHelper.parseBool(j, Field.ATTR_HIDDEN, false));
            fd.setRequired(RestHelper.parseBool(j, Field.ATTR_MANDATORY, false));
            fd.setTitle(RestHelper.parseString(j, RestHelper.ATTR_NAME));

            logger.info("FieldParser: resolved FieldType shortType='{}', value='{}', id={}",
                    fd.getFieldType().getShortType(),
                    fd.getFieldType().getValue(),
                    fd.getFieldType().getId());


            if (j.has(Field.ATTR_MEASURE_OPTIONS)) {
                parseMeasures(j.getAsJsonArray(Field.ATTR_MEASURE_OPTIONS), fd);
            }
            if (j.has(Field.ATTR_OPTIONS)) {
                parseOptions(j.getAsJsonArray(Field.ATTR_OPTIONS), fd);
            }
        }
        return fd;
    }

    /**
     * JSON object is for Material fields extraction
     * Parse field definition as appropriate for containers, ...
     * @param def
     * @param fd
     */
    private void parseDefinition(JsonObject def, Field fd) {
        fd.setAttributeListEid(RestHelper.parseString(def, Field.ATTR_ATTRIBUTE_LIST_EID));
        fd.setDefaultUnit(RestHelper.parseString(def, Field.ATTR_DEFAULT_UNIT));
        fd.setDefinedBy(RestHelper.parseString(def, Field.ATTR_DEFINED_BY));
        fd.setFieldType(lookupFieldType(RestHelper.parseString(def, Field.ATTR_FIELD_TYPE)));
        fd.setKey(RestHelper.parseString(def, Field.ATTR_KEY));
        fd.setTitle(RestHelper.parseString(def, Field.ATTR_TITLE));
        fd.setHidden(RestHelper.parseBool(def, Field.ATTR_HIDDEN, false));
        fd.setMultiSelect(RestHelper.parseBool(def, Field.ATTR_MULTISELECT, false));
        fd.setRequired(RestHelper.parseBool(def, Field.ATTR_REQUIRED, false));
        fd.setUserDefined(RestHelper.parseBool(def, Field.ATTR_USER_DEFINED, false));

        if (def.has(Field.ATTR_MEASURES)) {
            parseMeasures(def.getAsJsonArray(Field.ATTR_MEASURES), fd);
        }

        if (def.has(Field.ATTR_OPTIONS)) {
            parseOptions(def.getAsJsonArray(Field.ATTR_OPTIONS), fd);
        }
    }

    private void parseMeasures(JsonArray jArray, Field fd) {
        Iterator<JsonElement> iter = jArray.iterator();
        while (iter.hasNext()) {
            JsonObject j = iter.next().getAsJsonObject();
            String measure = j.getAsJsonPrimitive(Quality.ATTR_MEASURE).getAsString();
            Quality q = MeasureMapper.getQuality(measure);
            fd.addMeasure(new FieldMeasure(fd.getId(), q));
        }
    }

    private void parseOptions(JsonArray jArray, Field fd) {
        Iterator<JsonElement> iter = jArray.iterator();
        while (iter.hasNext()) {
            String option = iter.next().getAsJsonPrimitive().getAsString();
            fd.addOption(new FieldOption(fd.getId(), option));
        }
    }
}
