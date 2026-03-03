#!/usr/bin/env bash
#
# Start the database container
#
#==========================================================
#
p=`dirname $0`
INSTALL_DIR=`realpath "$p/.."`

cd $INSTALL_DIR/docker

#
case $1 in
        start)
                docker compose -f compose.yaml -f "$INSTALL_DIR/conf/compose.override.yaml" up -d
                ;;
        stop)
                docker compose down
                ;;
        restart)
                docker compose down
                docker compose -f compose.yaml -f "$INSTALL_DIR/conf/compose.override.yaml" up -d
                ;;

        *)
                echo "Usage: db.sh [start|stop|restart]"
                ;;
esac
