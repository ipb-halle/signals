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

package de.ipb_halle.signals.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.rest.RestHelper;
import de.ipb_halle.signals.rest.RestReplyParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class SignalsChildParser implements RestReplyParser<SignalsEntityDTO> {

    private DynEnumManager dynEnumManager;
    private Logger logger = LoggerFactory.getLogger(SignalsChildParser.class);

    public SignalsChildParser(DynEnumManager dem) {
        dynEnumManager = dem;
    }

    @Override
    public SignalsEntityDTO parseReply(JsonElement json) throws Exception {
        JsonObject jsonObj = json.getAsJsonObject();
        JsonObject jsonAttributes = jsonObj.getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);

        SignalsEntityDTO dto = new SignalsEntityDTO();
        dto.setId(RestHelper.parseString(jsonAttributes, SignalsEntityDTO.ATTR_EID));
        dto.setEid(RestHelper.parseString(jsonAttributes, SignalsEntityDTO.ATTR_EID));
        dto.setName(RestHelper.parseString(jsonAttributes, RestHelper.ATTR_NAME));
        dto.setDescription(RestHelper.parseString(jsonAttributes, RestHelper.ATTR_DESCRIPTION));
        dto.setType((EntityType) dynEnumManager.valueOf(EntityType.valueOf(RestHelper.parseString(jsonAttributes, RestHelper.ATTR_TYPE))));
        dto.setDigest(RestHelper.parseLong(jsonAttributes, RestHelper.ATTR_DIGEST));
        SignalsEntityRestService.parseTimestamps(jsonAttributes, dto);
        return dto;
    }
}
