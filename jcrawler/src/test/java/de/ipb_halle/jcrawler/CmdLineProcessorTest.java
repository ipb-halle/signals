/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 *
 */
package de.ipb_halle.jcrawler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
/**
 *
 * @author fblocal
 */
public class CmdLineProcessorTest {

    @Test
    public void testOptions() {
        Assertions.assertTrue(
                CmdLineProcessor.processCommandLine(
                new String[] {"-c", "nonexistent file"}));

        CmdLineProcessor.failWithoutConfigFile = false;

        // namespace missing
        Assertions.assertTrue(
                CmdLineProcessor.processCommandLine(
                new String[] {"-j", "purge"}));

        Assertions.assertFalse(
                CmdLineProcessor.processCommandLine(
                new String[] {"-j", "purge", "-n", "test"}));

        Assertions.assertTrue(
                CmdLineProcessor.processCommandLine(
                new String[] {"-j", "NONSENSE", "-n", "test"}));

        Assertions.assertTrue(
                CmdLineProcessor.processCommandLine(
                new String[] {"-h", "-f"}));

        Assertions.assertTrue(
                CmdLineProcessor.processCommandLine(
                new String[] {"-n"}));

        Assertions.assertTrue(
                CmdLineProcessor.processCommandLine(
                new String[] {"-l"}));

        Assertions.assertTrue(
                CmdLineProcessor.processCommandLine(
                new String[] {"-l", "semiconductor"}));

        Assertions.assertFalse(
                CmdLineProcessor.processCommandLine(
                new String[] {"-l", "WARN"}));

        Assertions.assertFalse(
                CmdLineProcessor.processCommandLine(
                new String[] {"-l", "DEBUG", "-f"}));
    }
}
