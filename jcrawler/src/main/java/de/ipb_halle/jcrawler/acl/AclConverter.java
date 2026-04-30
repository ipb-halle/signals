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
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author fblocal
 */
public class AclConverter {

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

    public final static int RAW_BUFFER_SIZE = 4096;

    private final DbPrincipalCache principalCache;
    private final static AclConverter parser = new AclConverter();

    private AclConverter() {
        principalCache = DbPrincipalCache.getInstance();
    }

    public static AclConverter getInstance() {
        return parser;
    }

    public byte[] buildRawAttribute(List<AclEntry> acl) {
        byte[] rawBuffer = new byte[RAW_BUFFER_SIZE];
        ByteBuffer buf = ByteBuffer.wrap(rawBuffer);
        buf.putInt(acl.size());
        for (AclEntry ace : acl) {
            addAce(buf, ace);
        }
        return Arrays.copyOf(buf.array(), buf.position());
    }

    private void addAce(ByteBuffer buf, AclEntry ace) {
        DbPrincipal principal = new DbPrincipal(ace.principal());
        principal = principalCache.lookup(principal);
        addAceType(buf, ace);
        addAceFlags(buf, ace, principal.isGroup());
        addAcePermissions(buf, ace);
        addPrincipal(buf, principal);
    }

    private void addAceType(ByteBuffer buf, AclEntry ace) {
        switch(ace.type()) {
            case ALLOW -> buf.putInt(NFS4_ACE_ACCESS_ALLOWED_ACE_TYPE);
            case DENY -> buf.putInt(NFS4_ACE_ACCESS_DENIED_ACE_TYPE);
            case AUDIT -> buf.putInt(NFS4_ACE_SYSTEM_AUDIT_ACE_TYPE);
            case ALARM -> buf.putInt(NFS4_ACE_SYSTEM_ALARM_ACE_TYPE);
            default -> throw new IllegalArgumentException("Unknown ACE type %s".formatted(ace.type().toString()));
        }
    }

    private void addAceFlags(ByteBuffer buf, AclEntry ace, boolean isGroup) {
        int flags = 0;
        for (AclEntryFlag f: ace.flags()) {
            switch (f) {
                case FILE_INHERIT -> flags |= NFS4_ACE_FILE_INHERIT_ACE;
                case DIRECTORY_INHERIT -> flags |= NFS4_ACE_DIRECTORY_INHERIT_ACE;
                case NO_PROPAGATE_INHERIT -> flags |= NFS4_ACE_NO_PROPAGATE_INHERIT_ACE;
                case INHERIT_ONLY -> flags |= NFS4_ACE_INHERIT_ONLY_ACE;
            }
        }
        if (isGroup) {
            flags |= NFS4_ACE_GROUP;
        }
        buf.putInt(flags);
    }

    private void addAcePermissions(ByteBuffer buf, AclEntry ace) {
        int permissions = 0;
        for (AclEntryPermission p : ace.permissions()) {
            switch (p) {
                case READ_DATA -> permissions |= NFS4_ACE_READ_DATA;
                case WRITE_DATA -> permissions |= NFS4_ACE_WRITE_DATA;
                case APPEND_DATA -> permissions |= NFS4_ACE_APPEND_DATA;
                case READ_NAMED_ATTRS -> permissions |= NFS4_ACE_READ_NAMED_ATTRS;
                case WRITE_NAMED_ATTRS -> permissions |= NFS4_ACE_WRITE_NAMED_ATTRS;
                case EXECUTE -> permissions |= NFS4_ACE_EXECUTE;
                case DELETE_CHILD -> permissions |= NFS4_ACE_DELETE_CHILD;
                case READ_ATTRIBUTES -> permissions |= NFS4_ACE_READ_ATTRIBUTES;
                case WRITE_ATTRIBUTES -> permissions |= NFS4_ACE_WRITE_ATTRIBUTES;
                case DELETE ->  permissions |= NFS4_ACE_DELETE;
                case READ_ACL -> permissions |= NFS4_ACE_READ_ACL;
                case WRITE_ACL -> permissions |= NFS4_ACE_WRITE_ACL;
                case WRITE_OWNER -> permissions |= NFS4_ACE_WRITE_OWNER;
                case SYNCHRONIZE -> permissions |= NFS4_ACE_SYNCHRONIZE;
            }
        }
        buf.putInt(permissions);
    }

    public void addPrincipal(ByteBuffer buf, Principal principal) {
        byte[] principalBytes = principal.getName().getBytes();
        int whoLength = principalBytes.length;
        int padLength = ((whoLength & 3) == 0) ? 0 : (4 - (whoLength & 3));
        buf.putInt(whoLength);
        buf.put(principalBytes);
        buf.put(new byte[padLength]);
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
