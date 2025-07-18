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
package de.ipb_halle.signals.users;

import de.ipb_halle.signals.SignalsConfig;
import de.ipb_halle.signals.UpdateConfig;
import de.ipb_halle.signals.reporting.HtmlList;
import de.ipb_halle.signals.reporting.HtmlReport;
import de.ipb_halle.signals.reporting.HtmlText;
import de.ipb_halle.signals.reporting.MailReport;
import java.util.Iterator;
import java.util.Map;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.logging.Level;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manage roles, groups and users from SNB, database and LDAP
 */
@Stateless
public class AccessManager {

    public final static String SECTION_HEADER = "header";
    public final static String SECTION_NEW_USERS = "new_users";
    public final static String SECTION_DISABLED_USERS = "disabled_users";
    public final static String SECTION_GROUP_INFO = "group_info";
    public final static String SECTION_ERRORS = "errors";

    @Resource
    private SignalsConfig signalsConfig;

    @Inject
    private GroupManager groupManager;

    @Inject
    private RoleManager roleManager;

    @Inject
    private UserManager userManager;

    private Logger logger = LoggerFactory.getLogger(AccessManager.class);

    public void manageAccess(UpdateConfig updateConfig, boolean noMail) {
        UserSynchronizationContext context = new UserSynchronizationContext(updateConfig);
        MailReport report = new MailReport();
        prepareReport(context, report);
        syncDbFromSnb(context);
        if (updateConfig.updateFromLdap) {
            syncSnbFromLdap(context);
        } else {
            logger.debug("DRY RUN: skipping updates from LDAP");
        }
        if (! noMail) {
            sendReport(context, report);
        }
    }

    protected static void prepareReport(UserSynchronizationContext context, HtmlReport report) {
        report.addSection(SECTION_HEADER, getReportHeader(context));
        report.addSection(SECTION_NEW_USERS, new HtmlList(
            "New and Reactivated Accounts",
            "The following list of users has been (re-)discovered in LDAP and subsequently added or reactivated in Signals Notebook:"
            ));
        report.addSection(SECTION_DISABLED_USERS, new HtmlList(
            "Disabled Accounts",
            "The following list of accounts are no longer allowed to access Signals Notebook:"
            ));
        report.addSection(SECTION_GROUP_INFO, new HtmlList(
            "Group Info",
            "Summary of changes in group membership:"
            ));
        report.addSection(SECTION_ERRORS, new HtmlList(
            "Errors",
            "Summary of severe errors, requiring external fixing:"
            ));
        context.report = report;
    }

    private static HtmlText getReportHeader(UserSynchronizationContext context) {
        if (context.updateConfig.updateSNB) {
            return new HtmlText(
                "Access Management",
                "This mail informs about changes to user accounts in Signals Notebook.");
        } else {
            return new HtmlText(
                "Access Management Dry Run!",
                "Signals Tool has not been allowed to make any changes. This mail informs about changes that will be made once the tool is allowed to make changes.");
        }
    }

    private void sendReport(UserSynchronizationContext context, MailReport report) {
        if (context.reportRecords > 0) {
            // quick and dirty to save configuration variables
            try {
                if (context.reportAlert) {
                    report.setSubject("ALERT: Signals Tool Summary - need fix");
                } else {
                    report.setSubject("Signals Tool Summary");
                }
                report.setSmtpProtocol("smtp")
                    .setSmtpPort(25)
                    .setSmtpHost("localhost")
                    .setFrom(signalsConfig.getMailFrom())
                    .setRecipient(signalsConfig.getMailTo())
                    .send();
            } catch (Exception e) {
                logger.warn("sendReport() received an exception", (Throwable) e);
            }
        }
    }

    private void syncDbFromSnb(UserSynchronizationContext context) {
        if (context.updateConfig.syncDbFromSNB) {
            roleManager.syncDbRolesFromSnb(context);
            groupManager.syncDbGroupsFromSnb(context.updateConfig);
            userManager.syncDbUsersFromSnb(context.updateConfig);
        } else {
            logger.debug("Skipping Db syncronization from SNB");
        }
    }

    private void syncSnbFromLdap(UserSynchronizationContext context) {
        roleManager.obtainStandardUserRole(context);
        try {
            roleManager.obtainLdapRoles(context);
            groupManager.obtainLdapGroups(context);
            userManager.syncUsersFromLdap(context);
        } catch(LdapConnectionErrorException lcee) {
            reportSyncSnbFromLdapException(context, lcee);
        } catch (NamingException ne) {
            reportSyncSnbFromLdapException(context, ne);
        } catch (MissingAttributeException mae) {
            reportSyncSnbFromLdapException(context, mae);
        } catch (IOException ioe) {
            reportSyncSnbFromLdapException(context, ioe);
        }
    }

    private void reportSyncSnbFromLdapException(UserSynchronizationContext context,
            Throwable exception) {
        logger.warn("syncUsersFromLdap() caught an exception: ", (Throwable) exception);
        context.report.addContent(AccessManager.SECTION_ERRORS,
                String.format("%s (see logs) during LDAP user synchronization: %s",
                        exception.getClass().getName(),
                        exception.getMessage()));
        context.reportRecords++;
        context.reportAlert = true;
    }

}
