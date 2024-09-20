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
package de.ipb_halle.signals.reporting;

import j2html.tags.Tag;
import static j2html.TagCreator.*;


public class HtmlText implements HtmlSection {

    private String title;
    private String text;

    public HtmlText(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public void addContent(String content) {
    }

    public Tag getContent() {
        return div(h1(title), p(text));
    }
}
