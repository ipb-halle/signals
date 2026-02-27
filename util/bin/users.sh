#!/bin/bash
#
# Synchronizes users among SNB, DB and LDAP
#
#
p=`dirname $0`
DIR=`realpath "$p/.."`
LOCKFILE=$DIR/logs/users.lock

function error {
	echo "Error: unknown option $1"
	exit 1
}

function printHelp {
	echo "Usage: users.sh [-h|--help] [--noSyncDbFromSNB]"
}

#
#==========================================================
#

GETOPT=$(getopt -o 'h' --longoptions 'help,noSyncDbFromSNB' -n 'users.sh' -- "$@")
if [ $? -ne 0 ]; then
        echo 'Error in commandline evaluation. Terminating...' >&2
        exit 1
fi

eval set -- "$GETOPT"
unset GETOPT
OPTIONS=""

while true ; do
	case "$1" in
		'--help')
			printHelp
			exit 0
			;;
		'--noSyncDbFromSNB')
			OPTIONS="${OPTIONS} $1"
			shift
			continue
			;;
		'--')
			shift
			break;
			;;
		*)
			error "$1"
	esac
done


cd $DIR
if [ -e $LOCKFILE ] ; then
	error "Found lock file: user management job is already running."
fi
touch $LOCKFILE

java -jar $DIR/lib/signals-1.0-jar-with-dependencies.jar \
	--config $DIR/conf/production_openejb.xml \
	--trustStore $DIR/conf/truststore.jks \
	--debug DEBUG \
	$OPTIONS \
	--manage-users 

rm $LOCKFILE

