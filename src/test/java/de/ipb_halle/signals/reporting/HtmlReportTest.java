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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class HtmlReportTest {

    @Test
    public void reportTest() {
        HtmlReport report = new HtmlReport();

        HtmlSection pageHeader = new HtmlText("Report Demo", "Just a few introductory words.");

        HtmlList sectionOne = new HtmlList("People", "People from history");
        sectionOne.addContent("Julius Caesar");
        sectionOne.addContent("Napoleon Bonaparte");
        sectionOne.addContent("George Washington");
        sectionOne.addContent("Charles de Gaulle");
        sectionOne.addContent("Winston Churchill");

        HtmlList sectionTwo = new HtmlList("Oceans", "Oceans of the world");

        report.addSection("header", pageHeader);
        report.addSection("one", sectionOne);
        report.addSection("two", sectionTwo);

        report.addContent("two", "Atlantic Ocean");
        report.addContent("two", "Pacific Ocean");
        report.addContent("two", "Indian Ocean");

        String reportString = report.render();
        Assertions.assertTrue(reportString.contains("few introductory words"), "Report contains heading");
        Assertions.assertTrue(reportString.contains("from history</div><ul><li>Julius Caesar</li>"), "Report contains Caesar");
        Assertions.assertTrue(reportString.contains("<li>Indian Ocean</li></ul></p></body></html>"), "Report contains Indian Ocean");
    }
}
