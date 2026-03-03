#!/bin/bash
#
# Creates a backup of signals data
#
#
#==========================================================
#
p=`dirname $0`
INSTALL_DIR=`realpath "$p/.."`
CONFIG=$INSTALL_DIR/conf/default.conf

function printHelp {
        echo "Usage: backup.sh [-h|--help] [-c|--config CONFIG]"
        echo
        echo "-c|--config CONFIG"
        echo "    Configuration data for the execution environment"
        echo
        echo "-h|--help"
        echo "    Print this help text."
}

function error {
        echo "Error: $1"
	echo
	printHelp
        exit 1
}
#
#==========================================================
#

GETOPT=$(getopt -o 'hc:' --longoptions 'help,config:' -n 'backup.sh' -- "$@")
if [ $? -ne 0 ]; then
        echo 'Error in commandline evaluation. Terminating...' >&2
        exit 1
fi

eval set -- "$GETOPT"
unset GETOPT
OPTIONS=""

while true ; do
        case "$1" in
                '-h'|'--help')
                        printHelp
                        exit 0
                        ;;
                '-c'|'--config')
                        CONFIG="$2"
                        shift 2
                        continue
                        ;;
                '--')
                        shift
                        break;
                        ;;
                *)
                        error "unknown option $1"
        esac
done

#
#==========================================================
#
cd $INSTALL_DIR
if [ -e $CONFIG ] ; then
    . $CONFIG
else
    error "Could not find config file."
fi


DATE=`date +%Y%m%d_%H%M%S`

tar -czf "$BACKUP_DIR/data.$DATE.tar.gz" $INSTALL_DIR $DATA_DIR

pg_dump -h $DB_HOST -U $DB_USER $DB_NAME | \
                gzip > "$BACKUP_DIR/db.$DATE.sql.gz"

find "$BACKUP_DIR" -type f -mtime +7 -exec rm {} \;

