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

import java.util.Properties;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;


public class MailReport extends HtmlReport {

    private String recipient;
    private String subject;
    private String from;
    private String smtpUser;
    private String smtpPassword;
    private String smtpHost;
    private int smtpPort;
    private String smtpProtocol;

    public MailReport() {
        super();
        smtpPort = 25;
        smtpProtocol = "smtp";
    }

    public void send() throws Exception {
        Properties props = getProperties();
        Session session = Session.getDefaultInstance(props);

        try (Transport transport = session.getTransport()) {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            msg.setSubject(subject);
            msg.setContent(render(), "text/html; charset=utf-8");

            transport.connect(smtpHost, smtpPort, smtpUser, smtpPassword);
            transport.sendMessage(msg, msg.getAllRecipients());
        }
    }

    private Properties getProperties() {
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", smtpProtocol);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.auth", "false");

//      props.put("mail.smtp.auth.login.disable", "true");      // default authorization order is "LOGIN PLAIN DIGEST-MD5 NTLM".
                                                                // 'LOGIN' must be disabled since Email Delivery authorizes as 'PLAIN'

//      props.put("mail.smtp.auth", "true");
//      props.put("mail.smtp.ssl.enable", "true");              // default value is false if not set
//      props.put("mail.smtp.ssl.protocols", "TLSv1.2");        // New Line
//      props.put("mail.smtp.ssl.trust", mailUri.getHost());
        props.put("mail.smtp.starttls.enable", "false");         //TLSv1.2 is required
//      props.put("mail.smtp.starttls.required", "true");

        return props;
    }

    public MailReport setFrom(String f) {
        from = f;
        return this;
    }

    public MailReport setRecipient(String r) {
        recipient = r;
        return this;
    }

    public MailReport setSubject(String s) {
        subject = s;
        return this;
    }

    public MailReport setSmtpUser(String u) {
        smtpUser = u;
        return this;
    }

    public MailReport setSmtpPassword(String p) {
        smtpPassword = p;
        return this;
    }

    public MailReport setSmtpHost(String h) {
        smtpHost = h;
        return this;
    }

    /**
     * set alternative port number (e.g. 587); default port number is 25
     */
    public MailReport setSmtpPort(int p) {
        smtpPort = p;
        return this;
    }

    /**
     * set alternative protocol (e.g. "smtps"); default protocol is "smtp"
     */
    public MailReport setSmtpProtocol(String p) {
        smtpProtocol = p;
        return this;
    }
}
