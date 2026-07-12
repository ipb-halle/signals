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
#include "de_ipb_halle_jcrawler_linux_LinuxCalls.h"


#define ERROR_PATH      -10001
#define ERROR_ATTR      -10002
#define ERROR_BUFFER    -10003

static int getString(JNIEnv *env, jstring jstr, const char** cstr) {
    *cstr = (*env)->GetStringUTFChars(env, jstr, NULL);
    if (cstr == NULL) {
        return -1;
    }
    return 0;
}

static void releaseString(JNIEnv *env, jstring jstr, const char* cstr) {
    (*env)->ReleaseStringUTFChars(env, jstr, cstr);
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

jint JNICALL Java_de_ipb_1halle_jcrawler_linux_LinuxCalls_readAttribute
  (JNIEnv *env, jclass callingClass, jstring jfilename, jstring jattrname, jbyteArray jbytes) {

    const char *path;
    const char *attr;
    char *buffer;
    int length;
    int result = 0;

    if (getString(env, jfilename, &path)) {
        result = ERROR_PATH;
        goto errorReturn;
    }

    if (getString(env, jattrname, &attr)) {
        result = ERROR_ATTR;
        goto errorCleanFile;
    }

    if (getBuffer(env, jbytes, &buffer, &length)) {
        result = ERROR_BUFFER;
        goto errorCleanAttr;
    }

    result = lgetxattr(path, attr, buffer, length);
    if (result < 0) {
        result = -errno;
    }

    releaseBuffer(env, jbytes, buffer);
errorCleanAttr:
    releaseString(env, jattrname, attr);
errorCleanFile:
    releaseString(env, jfilename, path);
errorReturn:
    return result;
}

