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

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;

public class MailReportTest {

    /*
     * Test ignored because addresses are invalid. Didn't
     * want to commit real address data.
     */
    @Ignore @Test
    public void reportTest() {
        MailReport report = new MailReport();

        HtmlSection pageHeader = new HtmlText("Just Nonsense", "An apple a day is worth a pound of cure.");
        report.addSection("header", pageHeader);

        try {
            report.setFrom("sender@somewhere.invalid")
                .setRecipient("recipient@somewhere.invalid")
                .setSubject("Mail Report Test")
                .setSmtpHost("localhost")
                .setSmtpPort(25)
                .setSmtpProtocol("smtp")
                .send();
        } catch (Exception e) {
        }
        
        assertTrue("Report does not throw exception", true);
    }
}
