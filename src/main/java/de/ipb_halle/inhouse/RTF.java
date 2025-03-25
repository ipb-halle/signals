/*
 * Signals Tool
 * Copyright 2025 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.inhouse;

import com.rtfparserkit.parser.RtfListenerAdaptor;
import com.rtfparserkit.parser.RtfStringSource;
import com.rtfparserkit.parser.raw.RawRtfParser;
import com.rtfparserkit.rtf.Command;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * RTF-Parser
 * This parser can extract text data from files exported from
 * ChemFinder(tm) or MicroSoft(R) Access(tm). In these files
 * plain text records (UTF-8) are mixed with records containing
 * RTF. This class converts all text to UTF-8 HTML, taking
 * special cases (like greek letters) into account. However,
 * some conversion (see below) (and manual curation) of the raw
 * export is necessary.
 * <p>
 * Data preparation: conversion of 8bit characters
 * <p>
 * iconv -f ISO8859-1 -t UTF-8 -o OUTPUT.txt ../INPUT.txt
 * <p>
 * followed by manual curation. This code was originally
 * developed by in 2014 for KICKS.
 *
 * @author: fbroda
 */
public class RTF extends RtfListenerAdaptor {

    private final static String MATERIAL_NAME_DEFAULT_LANG = "en";

    public enum MODE {FONTDEF, IGNORE, TEXTOUT}

    public enum LANG {NORMAL, GREEK}

    private final Map<Character, String> greekmap;

    private final InhouseDB inhouseDB;
    private String currentFont;
    private int depth;
    private Map<String, LANG> fontmap;
    private LANG lang;
    private MODE mode;
    private String nosupersub;
    private StringBuilder outString;

    private int recordCounter;

    public RTF(InhouseDB inhouseDB) {
        this.recordCounter = 0;
        this.inhouseDB = inhouseDB;
        this.greekmap = new HashMap<>();
        init();
    }

    private void init() {
        this.greekmap.put('a', "&#945;");
        this.greekmap.put('b', "&#946;");
        this.greekmap.put('g', "&#947;");
        this.greekmap.put('d', "&#948;");
        this.greekmap.put('D', "&#916;");
        this.greekmap.put('0', "0");
        this.greekmap.put('1', "1");
        this.greekmap.put('2', "2");
        this.greekmap.put('3', "3");
        this.greekmap.put('4', "4");
        this.greekmap.put('5', "5");
        this.greekmap.put('6', "6");
        this.greekmap.put('7', "7");
        this.greekmap.put('8', "8");
        this.greekmap.put('9', "9");
        this.greekmap.put('(', "(");
        this.greekmap.put(')', ")");
        this.greekmap.put('{', "{");
        this.greekmap.put('}', "}");
        this.greekmap.put('[', "[");
        this.greekmap.put(']', "]");
        this.greekmap.put('.', ".");
        this.greekmap.put(',', ",");
        this.greekmap.put(':', ":");
        this.greekmap.put('-', "-");
        this.greekmap.put('+', "+");
        this.greekmap.put('/', "/");
        this.greekmap.put('*', "*");
        this.greekmap.put(';', ";");
        this.greekmap.put('_', "_");
    }

    public void readCompoundSynonym(String fileName) throws Exception {
        System.out.println("Importing compound names");
        int lineMode = 0;
        String line = "";
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        reader.readLine(); // discard header

        Pattern pattern = Pattern.compile("^(\\d+);(\\d+);(.*);([NY])$");
        Pattern quotePattern = Pattern.compile("^\"(.*)\"$");

        while (reader.ready()) {
            String st = reader.readLine();

            st = st.replaceAll("\u00b2", "<sup>2</sup>")
                    .replaceAll("\u00b4", "'")
                    .replaceAll("\\\\''e1", "a")
                    .replaceAll("\\\\''e2", "b")
                    .replaceAll("\\\\''c4", "D")
                    .replaceAll("\\\\''b0", "&deg;")
                    .replaceAll("\\\\''d7", "x")
                    .replaceAll("\\\\f2\\\\''ec", "&micro;");

            line += st;
/*
            if (st.matches("^[0-9]+;[0-9]+;'\\{\\\\rtf1.*")) {
                // System.out.println("RTF MODE");
                lineMode = 1;
            }
            if ((lineMode == 1) && (st.matches("^';'Y'$") || st.matches("^';'N'$"))) {
                lineMode = 0;
                String nameId = line.replaceAll("^([0-9]+;[0-9]+;')(.*)(';'[NY]')$", "$1");
                String molId = line.replaceAll("^([0-9]+;[0-9]+;')(.*)(';'[NY]')$", "$2");
                String synonym = line.replaceAll("^([0-9]+;[0-9]+;')(.*)(';'[NY]')$", "$3");
                molId = molId.replaceAll("''", "'");
                // System.out.printf("DEBUG: %s\n", b);
                molId = readRTF(molId).replaceAll("'", "''");
                line = nameId + molId + synonym;

            }
 */
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                int nameId = Integer.parseInt(matcher.group(1));
                int molId = Integer.parseInt(matcher.group(2));
                String synonym = matcher.group(3);
                Matcher quoteMatcher = quotePattern.matcher(synonym);
                if (quoteMatcher.matches()) {
                    synonym = quoteMatcher.group(1);
                }
                boolean priority = matcher.group(4).matches("Y");

                if (synonym.equals("")) {
                    synonym = String.format("AUTO_molId%d", molId);
                }
                update(nameId, molId, synonym, priority);
                line = "";
            } else {
                System.out.printf("Line didn't match: %s\n", line);
            }
        }
        reader.close();
    }

    private void showProgress() {
        this.recordCounter++;
        if ((this.recordCounter % 500) == 0) {
            System.out.printf("imported %d compound names\n", this.recordCounter);
        }
    }

    /**
     * @param name_id  name record primary key in the inhouse database (ignored)
     * @param molId   reference id to structure
     * @param synonym     the quoted name (quotes are stripped)
     * @param prioFlag priority name
     * @throws Exception
     */
    private void update(int name_id, int molId, String synonym, boolean prioFlag) throws Exception {
        showProgress();
        InhouseDbService dbService = inhouseDB.getInhouseDbService();

        if (prioFlag) {
            // set the primary name to the compound priority name
            InhouseCompound compound = dbService.loadCompoundByMolId(molId);
            if (compound == null) {
                return;
            }
            compound.setName(synonym);
            dbService.save(compound);
        }
        dbService.save(new InhouseCompoundSynonym()
                .setSynonym(synonym)
                .setMolId(molId));
    }

    /*
     * read an RTF string
     */
    public String readRTF(String rfSrc) {
        RtfStringSource src = new RtfStringSource(rfSrc);
        RawRtfParser prs = new RawRtfParser();
        this.depth = 0;
        this.fontmap = new HashMap<String, LANG>();
        this.lang = LANG.NORMAL;
        this.mode = MODE.TEXTOUT;
        this.outString = new StringBuilder();
        try {
            prs.parse(src, this);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        String out = this.outString.toString().replaceAll("\\s+", " ");
        return out;
    }

    @Override
    @SuppressWarnings("fallthrough")
    public void processCommand(Command command, int parameter, boolean hasParameter, boolean optional) {
        /*
        if(hasParameter) {
                System.out.printf("command %s %d\n", command.getCommandName(), parameter);
        } else {
                System.out.printf("command %s\n", command.getCommandName());
        }
        */
        switch (command.getCommandName()) {
            case "b":
                if (this.mode == MODE.TEXTOUT) {
                /*
                 * bold is not needed.
                 * some entries use all bold font, this is cleaned up
                 *
                        if(hasParameter && (parameter == 0)) {
                                this.outString.append("</b>");
                        } else {
                                this.outString.append("<b>");
                        }
                */
                }
                break;
            case "colortbl":
                this.mode = MODE.IGNORE;
                break;
            case "f":
                this.currentFont = String.format("f%d", parameter);
                if (this.mode == MODE.TEXTOUT) {
                    this.lang = this.fontmap.get(this.currentFont);
                } else {
                    this.fontmap.put(this.currentFont, LANG.NORMAL);
                }
                break;
            case "fcharset":
                switch (parameter) {
                    case 0:
                        this.fontmap.put(this.currentFont, LANG.NORMAL);
                        break;
                    case 2:
                        this.fontmap.put(this.currentFont, LANG.GREEK);
                        break;
                    case 161:
                        this.fontmap.put(this.currentFont, LANG.GREEK);
                        break;
                    default:
                        System.out.printf("ERROR: illegal charset %d\n", parameter);
                }
                break;
            case "fonttbl":
                this.mode = MODE.FONTDEF;
                break;
            case "i":
                if (this.mode == MODE.TEXTOUT) {
                    if (hasParameter && (parameter == 0)) {
                        this.outString.append("</i>");
                    } else {
                        this.outString.append("<i>");
                    }
                }
                break;
            case "lang":
            /*
             * ignore lang parameter (not conclusive)
             *
                switch(parameter) {
                    case 1031 :
                    case 1033 :
                    case 1036 :
                    case 2057 :
                        this.lang = LANG.NORMAL;
                        break;
                    case 1040 :
                        this.lang = LANG.GREEK;
                        break;
                    default :
                        System.out.printf("ERROR: illegal lang %d\n", parameter);
                }
            */
                break;
            case "nosupersub":
                this.outString.append(this.nosupersub);
                break;
            case "sub":
                this.outString.append("<sub>");
                this.nosupersub = "</sub>";
                break;
            case "super":
                this.outString.append("<sup>");
                this.nosupersub = "</sup>";
                break;
            case "u":
                this.outString.append(String.format("&#%d;", parameter));
                break;
        }
    }

    @Override
    public void processDocumentEnd() {
        // System.out.println("DOCUMENT END");
    }

    @Override
    public void processDocumentStart() {
        // System.out.println("DOCUMENT START");
    }

    @Override
    public void processGroupStart() {
        // System.out.printf("GROUP START %d\n", this.depth);
        this.depth++;
    }

    @Override
    public void processGroupEnd() {
        this.depth--;
        // System.out.printf("GROUP END %d\n", this.depth);
        if (this.depth == 1) {
            this.mode = MODE.TEXTOUT;
        }
    }

    @Override
    public void processCharacterBytes(byte[] data) {
        String st = new String(data, StandardCharsets.UTF_8);
        if (this.mode == MODE.TEXTOUT) {
            if (this.lang == LANG.NORMAL) {
                this.outString.append(st);
            } else {
                for (int i = 0; i < st.length(); i++) {
                    char c = st.charAt(i);
                    String x = this.greekmap.get(c);
                    if (x != null) {
                        this.outString.append(x);
                    } else {
                        System.out.printf("ERROR: unknown character mapping in String %s\n", st);
                    }
                }
            }
        }
    }

    @Override
    public void processBinaryBytes(byte[] data) {
        this.outString.append("BINARY DATA");
    }

    @Override
    public void processString(String st) {
        if (this.lang == LANG.NORMAL) {
            this.outString.append(st);
        } else {
            this.outString.append("<ERROR:GREEK>");
            this.outString.append(st);
            this.outString.append("</ERROR:GREEK>");
        }
    }
}
