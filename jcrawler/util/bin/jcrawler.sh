#!/usr/bin/env bash
#
#
p=`dirname $0`
DIR=`realpath $p/..`
OPTIONS=$*

. $DIR/conf/jcrawler.conf

java -Djava.library.path=$DIR/$LIB_DIR/native \
    --enable-native-access=ALL-UNNAMED \
    -jar $DIR/$LIB_DIR/jcrawler-$VERSION-jar-with-dependencies.jar \
    $OPTIONS 
