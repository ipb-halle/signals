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

import java.util.ArrayList;
import java.util.List;
import static j2html.TagCreator.*;

public class HtmlReport {

    private List<HtmlSection> sections;

    public HtmlReport() {
        sections = new ArrayList<> ();
    }

    public HtmlReport addSection(HtmlSection section) {
        sections.add(section);
        return this;
    }

    public String render() {
        return document(
            html(
                head(
                    title("SignalsTool HtmlReport")
                ),
                body(
                    each(sections, section -> section.getContent())
                )
            )
        );
    }
}
