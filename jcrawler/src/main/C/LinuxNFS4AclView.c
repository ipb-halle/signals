/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
#include <errno.h>
#include <sys/xattr.h>
#include <jni.h>
#include "de_ipb_halle_jcrawler_acl_LinuxNFS4AclView.h"


#define NFS4_ACL_ATTR "system.nfs4_acl"
#define ERROR_PATH      -10001
#define ERROR_BUFFER    -10002

static int getPath(JNIEnv *env, jstring jstr, const char** path) {
    *path = (*env)->GetStringUTFChars(env, jstr, NULL);
    if (path == NULL) {
        return -1;
    }
    return 0;
}

static void releasePath(JNIEnv *env, jstring jstr, const char* path) {
    (*env)->ReleaseStringUTFChars(env, jstr, path);
}

static int getBuffer(JNIEnv *env, jbyteArray jbytes, char **buffer, int *length) {
    jboolean isCopy;                    // mandatory parameter
    *buffer = (*env)->GetByteArrayElements(env, jbytes, &isCopy);
    if (*buffer == NULL) {
        return -1;
    }

    *length = (*env)->GetArrayLength(env, jbytes);
    return 0;
}

static void releaseBuffer(JNIEnv *env, jbyteArray jbytes, char* buffer) {
    // 0 = Commit & free
    (*env)->ReleaseByteArrayElements(env, jbytes, buffer, 0);
}

jint JNICALL Java_de_ipb_1halle_jcrawler_acl_LinuxNFS4AclView_readAttribute
  (JNIEnv *env, jclass callingClass, jstring jstr, jbyteArray jbytes) {

    const char *path;
    char *buffer;
    int length;
    int result = 0;

    if (getPath(env, jstr, &path)) {
        result = ERROR_PATH;
        goto errorReturn;
    }

    if (getBuffer(env, jbytes, &buffer, &length)) {
        releasePath(env, jstr, path);
        result = ERROR_BUFFER;
        goto errorCleanString;
    }

    result = getxattr(path, NFS4_ACL_ATTR, buffer, length);
    if (result < 0) {
        result = -errno;
    }

errorCleanBuffer:
    releaseBuffer(env, jbytes, buffer);
errorCleanString:
    releasePath(env, jstr, path);
errorReturn:
    return result;
}

