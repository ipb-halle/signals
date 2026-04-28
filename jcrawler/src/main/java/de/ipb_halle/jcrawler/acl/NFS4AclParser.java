/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.acl;

import de.ipb_halle.jcrawler.db.DbPrincipal;
import de.ipb_halle.jcrawler.db.DbPrincipalCache;
import java.nio.ByteBuffer;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author fblocal
 */
public class NFS4AclParser {

    public final static int NFS4_ACE_ACCESS_ALLOWED_ACE_TYPE = 0;
    public final static int NFS4_ACE_ACCESS_DENIED_ACE_TYPE  = 1;
    public final static int NFS4_ACE_SYSTEM_AUDIT_ACE_TYPE   = 2;
    public final static int NFS4_ACE_SYSTEM_ALARM_ACE_TYPE   = 3;

    public final static int NFS4_ACE_FILE_INHERIT_ACE           = 0x00000001;
    public final static int NFS4_ACE_DIRECTORY_INHERIT_ACE      = 0x00000002;
    public final static int NFS4_ACE_NO_PROPAGATE_INHERIT_ACE   = 0x00000004;
    public final static int NFS4_ACE_INHERIT_ONLY_ACE           = 0x00000008;
    public final static int NFS4_ACE_SUCCESSFUL_ACCESS_ACE_FLAG = 0x00000010;
    public final static int NFS4_ACE_FAILED_ACCESS_ACE_FLAG     = 0x00000020;
    public final static int NFS4_ACE_IDENTIFIER_GROUP           = 0x00000040;
    public final static int NFS4_ACE_OWNER                      = 0x00000080;
    public final static int NFS4_ACE_GROUP                      = 0x00000100;
    public final static int NFS4_ACE_EVERYONE                   = 0x00000200;

    public final static int NFS4_ACE_READ_DATA                  = 0x00000001;
    public final static int NFS4_ACE_LIST_DIRECTORY             = 0x00000001;
    public final static int NFS4_ACE_WRITE_DATA                 = 0x00000002;
    public final static int NFS4_ACE_ADD_FILE                   = 0x00000002;
    public final static int NFS4_ACE_APPEND_DATA                = 0x00000004;
    public final static int NFS4_ACE_ADD_SUBDIRECTORY           = 0x00000004;
    public final static int NFS4_ACE_READ_NAMED_ATTRS           = 0x00000008;
    public final static int NFS4_ACE_WRITE_NAMED_ATTRS          = 0x00000010;
    public final static int NFS4_ACE_EXECUTE                    = 0x00000020;
    public final static int NFS4_ACE_DELETE_CHILD               = 0x00000040;
    public final static int NFS4_ACE_READ_ATTRIBUTES            = 0x00000080;
    public final static int NFS4_ACE_WRITE_ATTRIBUTES           = 0x00000100;
    public final static int NFS4_ACE_DELETE                     = 0x00010000;
    public final static int NFS4_ACE_READ_ACL                   = 0x00020000;
    public final static int NFS4_ACE_WRITE_ACL                  = 0x00040000;
    public final static int NFS4_ACE_WRITE_OWNER                = 0x00080000;
    public final static int NFS4_ACE_SYNCHRONIZE                = 0x00100000;

    public final static String NFS4_OWNER = "OWNER@";
    public final static String NFS4_GROUP = "GROUP@";
    public final static String NFS4_EVERYONE = "EVERYONE@";


    private final DbPrincipalCache principalCache;
    private final static NFS4AclParser parser = new NFS4AclParser();

    private NFS4AclParser() {
        principalCache = DbPrincipalCache.getInstance();
    }

    public static NFS4AclParser getInstance() {
        return parser;
    }

    public List<AclEntry> parseAcl(byte[] rawBuffer) {
        List<AclEntry> acl = new ArrayList<> ();
        if (rawBuffer != null) {
            ByteBuffer buf = ByteBuffer.wrap(rawBuffer);
            int ptr = 0;
            int nacl = buf.getInt(ptr);
            ptr += 4;
            for (int i=0; i < nacl; i++) {
                AclEntry.Builder builder = AclEntry.newBuilder();
                parseAceType(builder, buf.getInt(ptr));
                boolean isGroup = parseAceFlags(builder, buf.getInt(ptr + 4));
                parseAcePermissions(builder, buf.getInt(ptr + 8));
                int wholen = buf.getInt(ptr + 12);
                ptr += 16;
                parseAcePrincipal(builder, new String(rawBuffer, ptr, wholen), isGroup);
                // align ptr to 32 bit word address
                ptr += (wholen & ~3) + (((wholen & 3) > 0) ? 4 : 0);
                acl.add(builder.build());
            }
        }
        return acl;
    }

    private void parseAceType(AclEntry.Builder builder, int type) {
        switch(type) {
            case NFS4_ACE_ACCESS_ALLOWED_ACE_TYPE -> builder.setType(AclEntryType.ALLOW);
            case NFS4_ACE_ACCESS_DENIED_ACE_TYPE -> builder.setType(AclEntryType.DENY);
            case NFS4_ACE_SYSTEM_AUDIT_ACE_TYPE -> builder.setType(AclEntryType.AUDIT);
            case NFS4_ACE_SYSTEM_ALARM_ACE_TYPE -> builder.setType(AclEntryType.ALARM);
            default -> throw new IllegalArgumentException("Parse error: unknown ACE type %d".formatted(type));
        }
    }

    private boolean parseAceFlags(AclEntry.Builder builder, int flags) {
        Set<AclEntryFlag> aceFlags = new HashSet<> ();

        if((flags & NFS4_ACE_FILE_INHERIT_ACE) > 0) {
            aceFlags.add(AclEntryFlag.FILE_INHERIT);
        }
        if((flags & NFS4_ACE_DIRECTORY_INHERIT_ACE) > 0) {
            aceFlags.add(AclEntryFlag.DIRECTORY_INHERIT);
        }
        if((flags & NFS4_ACE_NO_PROPAGATE_INHERIT_ACE) > 0) {
            aceFlags.add(AclEntryFlag.NO_PROPAGATE_INHERIT);
        }
        if((flags & NFS4_ACE_INHERIT_ONLY_ACE) > 0) {
            aceFlags.add(AclEntryFlag.INHERIT_ONLY);
        }
        // flags for AUDIT & ALARM are ignored

        if(! aceFlags.isEmpty()) {
            builder.setFlags(aceFlags);
        }

        // isGroup?
        return ((flags & NFS4_ACE_GROUP) > 0);
    }

    private void parseAcePermissions(AclEntry.Builder builder, int perm) {
        Set<AclEntryPermission> acePerms = new HashSet<> ();
        if ((perm & NFS4_ACE_READ_DATA) > 0) {
            acePerms.add(AclEntryPermission.READ_DATA);
        }
        if ((perm & NFS4_ACE_WRITE_DATA) > 0) {
            acePerms.add(AclEntryPermission.WRITE_DATA);
        }
        if ((perm & NFS4_ACE_APPEND_DATA) > 0) {
            acePerms.add(AclEntryPermission.APPEND_DATA);
        }
        if ((perm & NFS4_ACE_READ_NAMED_ATTRS) > 0) {
            acePerms.add(AclEntryPermission.READ_NAMED_ATTRS);
        }
        if ((perm & NFS4_ACE_WRITE_NAMED_ATTRS) > 0) {
            acePerms.add(AclEntryPermission.WRITE_NAMED_ATTRS);
        }
        if ((perm & NFS4_ACE_EXECUTE) > 0) {
            acePerms.add(AclEntryPermission.EXECUTE);
        }
        if ((perm & NFS4_ACE_DELETE_CHILD) > 0) {
            acePerms.add(AclEntryPermission.DELETE_CHILD);
        }
        if ((perm & NFS4_ACE_READ_ATTRIBUTES) > 0) {
            acePerms.add(AclEntryPermission.READ_ATTRIBUTES);
        }
        if ((perm & NFS4_ACE_WRITE_ATTRIBUTES) > 0) {
            acePerms.add(AclEntryPermission.WRITE_ATTRIBUTES);
        }
        if ((perm & NFS4_ACE_DELETE) > 0) {
            acePerms.add(AclEntryPermission.DELETE);
        }
        if ((perm & NFS4_ACE_READ_ACL) > 0) {
            acePerms.add(AclEntryPermission.READ_ACL);
        }
        if ((perm & NFS4_ACE_WRITE_ACL) > 0) {
            acePerms.add(AclEntryPermission.WRITE_ACL);
        }
        if ((perm & NFS4_ACE_WRITE_OWNER) > 0) {
            acePerms.add(AclEntryPermission.WRITE_OWNER);
        }
        if ((perm & NFS4_ACE_SYNCHRONIZE) > 0) {
            acePerms.add(AclEntryPermission.SYNCHRONIZE);
        }
        builder.setPermissions(acePerms);
    }

    private void parseAcePrincipal(AclEntry.Builder builder, String name, boolean isGroup) {
        DbPrincipal principal = new DbPrincipal(name);
        principal.setGroup(isGroup);
        if (NFS4_OWNER.equals(name)) {
            principal.setEveryone(true);
        }
        principal = principalCache.lookup(principal);
        builder.setPrincipal((UserPrincipal) principal);
    }

}
