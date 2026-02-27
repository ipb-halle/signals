#!/bin/bash
#
# Creates a backup of signals production data
#
#
#==========================================================
#
p=`dirname $0`
DIR=`realpath "$p/.."`
. $DIR/conf/backup.conf

DATE=`date +%Y%m%d_%H%M%S`

tar -C "$DATA_DIR" -czf "$BACKUP_DIR/data.$DATE.tar.gz" . 

pg_dump -h $DB_HOST -U $DB_USER $DB_NAME | \
                gzip > "$BACKUP_DIR/db.$DATE.sql.gz"

find "$BACKUP_DIR" -type f -mtime +7 -exec rm {} \;

#
#==========================================================
#


