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
import de.ipb_halle.signals.rest.RestHelper;
import de.ipb_halle.signals.rest.RestReplyParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ShareParser implements RestReplyParser<List<Share>> {

    private Logger logger = LoggerFactory.getLogger(ShareParser.class);

    @Override
    public List<Share> parseReply(JsonElement json) {
        List<Share> shares = new ArrayList<>();
        JsonArray jsonArray = json.getAsJsonObject().getAsJsonArray(RestHelper.ATTR_DATA);
        for (JsonElement j : jsonArray.asList()) {
            shares.add(parseSingleShare(j));
        }
        return shares;
    }

    private Share parseSingleShare(JsonElement json) {
        JsonObject jsonObj = json.getAsJsonObject();
        JsonObject attr = jsonObj.getAsJsonObject(RestHelper.ATTR_ATTRIBUTES);
        Share share = parseShareType(jsonObj);
        share.setIsAdmin(RestHelper.parseBool(attr, Share.ATTR_IS_ADMIN, false));
        share.setCanRead(RestHelper.parseBool(attr, Share.ATTR_CAN_READ, false));
        share.setCanWrite(RestHelper.parseBool(attr, Share.ATTR_CAN_WRITE, false));
        share.setHasFullControl(RestHelper.parseBool(attr, Share.ATTR_HAS_FULL_CONTROL, false));
        return share;
    }

    private Share parseShareType(JsonObject jsonObj) {
        JsonObject relations = jsonObj.getAsJsonObject(RestHelper.ATTR_RELATIONSHIPS);
        if (relations.has(Share.ATTR_GROUP)) {
            String groupId = RestHelper.getPrimitiveFromPath(relations, Share.ATTR_GROUP_ID).toString();
            GroupShare g = new GroupShare();
            g.setGroupId(groupId);
            return g;
        }
        if (relations.has(Share.ATTR_USER)) {
            String userId = RestHelper.getPrimitiveFromPath(relations, Share.ATTR_USER_ID).toString();
            UserShare u = new UserShare();
            u.setUserId(userId);
            return u;
        }
        throw new RuntimeException("Illegal share type");
    }
}
