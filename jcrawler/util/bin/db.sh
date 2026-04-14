#!/usr/bin/env bash
#
p=`dirname $0`
DIR=`realpath $p/../..`

POSTGRESQL_IMAGE=postgres:17

echo "Base directory: $DIR"
docker run -e POSTGRES_PASSWORD=secret \
        -p 5432:5432 \
        --name db \
        --volume $DIR/util/schema:/docker-entrypoint-initdb.d \
        $POSTGRESQL_IMAGE
