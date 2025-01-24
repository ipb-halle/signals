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

package de.ipb_halle.signals.element;

import com.google.gson.JsonElement;
import de.ipb_halle.signals.attachment.Attachment;
import de.ipb_halle.signals.attachment.AttachmentRestService;
import de.ipb_halle.signals.rest.RestClient;
import de.ipb_halle.signals.rest.RestReplyParser;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class ElementRestService implements RestReplyParser<Element> {

    private Logger logger = LoggerFactory.getLogger(ElementRestService.class);

    public Element parseReply(JsonElement json) {
        throw new RuntimeException("Not implemented.");
    }

    public Element doGetAttachment(String id) {
        return parseReply(null);
    }

}
