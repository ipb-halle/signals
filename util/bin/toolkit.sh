#!/bin/bash
#
# Toolkit to perform B trial instance to DB
#
#
p=`dirname $0`
INSTALL_DIR=`realpath "$p/.."`
CONFIG=$INSTALL_DIR/conf/default.conf


function printHelp {
	echo "Usage: toolkit.sh [-h|--help] [-c|--config CONFIG] [-t|--tookit OPTIONS]"
        echo
        echo "-c|--config CONFIG"
        echo "    Configuration data for the execution environment"
        echo
        echo "-h|--help"
        echo "    Print this help text."
        echo
        echo "--help2"
        echo "    Print the Signals Toolkit help"
        echo
        echo "-t|--toolkit OPTIONS"
        echo "    Passthrough options for the Signals Toolkit, e.g. -t '--dry-run --allow-discover'"
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

GETOPT=$(getopt -o 'hc:t:' --longoptions 'help,config:,help2,toolkit:' -n 'toolkit.sh' -- "$@")
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
                '--help2')
                        OPTIONS="${OPTIONS} --help"
                        shift
                        continue
                        ;;
                '-c'|'--config')
                        CONFIG=$2
                        shift 2
                        continue
                        ;;
                '-t'|'--toolkit')
                        OPTIONS="${OPTIONS} $2"
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


cd $INSTALL_DIR
if [ -e $CONFIG ] ; then
    . $CONFIG
else
    error "Could not find config file."
fi

cd $DATA_DIR
if [ -e $LOCKFILE ] ; then
	error "Found lock file: syncronization job is already running."
fi
touch $LOCKFILE

java -jar $INSTALL_DIR/lib/signals-$VERSION-jar-with-dependencies.jar \
	--config $OPENEJB_XML \
	--trustStore $TRUSTSTORE \
	--debug INFO \
	$OPTIONS

#        -eS 2020-01-01:2022-03-31

rm $LOCKFILE

