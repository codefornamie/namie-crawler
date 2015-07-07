#!/bin/sh
PG_NAME=`basename $0 '.sh'`
PG_HOME=$( cd $(dirname $0)/../ ; pwd -P )
LIB_HOME="${PG_HOME}/lib"
CRAWLER_JAR=`ls -1 ${LIB_HOME}/namie-crawler-*.jar`

LOG_MAX_SIZE=10000000

CONF_FILE=${PG_HOME}/${PG_NAME}.conf

# Crowler JAR filename is defined as a head to detect logback configuration file that contained.
NAM_JARS="${CRAWLER_JAR}:"
for i in $(ls -1 ${LIB_HOME}/*.jar); do NAM_JARS="${NAM_JARS}${i}:"; done

if [ $# -ne 1 ]; then
  echo "RSSまたはRadiation引数が必要です。"
  exit 1
fi
CMD=$1

if [ -r "$CONF_FILE" ]; then
    . $CONF_FILE
fi

if [ -z "$TMP_DIR" ]; then
    TMP_DIR=/var/log/namie-crawler
fi

if [ -z "$BOOTSTRAP_CLASS" ]; then
    BOOTSTRAP_CLASS=jp.fukushima.namie.town.NamieCrawler
fi

if [ -z "$DMP_DIR" ]; then
    DMP_DIR=${TMP_DIR}
fi

if [ -z "$DMP_FILE" ]; then
    DMP_FILE=${DMP_DIR}/${PG_NAME}.log
fi

#---------------------------------
# check duplicate execute
#---------------------------------
LOCK_FILE=$TMP_DIR/.${PG_NAME}.lock
echo $LOCK_FILE

if [ -f $LOCK_FILE ]; then
    echo "$PG_NAME is already executed."
    exit 1
fi
echo $$ > $LOCK_FILE
trap 'rm -r $LOCK_FILE' 0 1 2 3 10 15

#---------------------------------
# run
#---------------------------------
java -cp $NAM_JARS ${BOOTSTRAP_CLASS} $CMD >> $DMP_FILE 2>&1

#---------------------------------
# rotate log
#---------------------------------
if [ `wc -c < $DMP_FILE` -gt $LOG_MAX_SIZE ]; then
    mv $DMP_FILE $DMP_FILE.0
    touch $DMP_FILE
fi
