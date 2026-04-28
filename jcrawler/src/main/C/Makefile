#
# SPDX-License-Identifier: Apache-2.0
# SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
#
# JCrawler
# JCrawler is a project to efficiently crawl large file systems.
#
#======================================================================
#
# To be called by Maven!
#
CFLAGS=-fPIC -c -I${JNI_INCLUDE} -I${JDK_HOME}/include -I${JDK_HOME}/include/linux
LDFLAGS=-shared

${BUILD_DIR}/libLinuxNFS4Acl.${VERSION}.so: LinuxNFS4AclHandler.c
	mkdir -p ${BUILD_DIR}
	${CC} ${CFLAGS} -o ${BUILD_DIR}/LinuxNFS4AclHandler.o LinuxNFS4AclHandler.c
	${CC} ${LDFLAGS} -o ${BUILD_DIR}/libLinuxNFS4Acl.${VERSION}.so ${BUILD_DIR}/LinuxNFS4AclHandler.o
