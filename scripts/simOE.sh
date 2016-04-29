#!/bin/sh

echo "Starting an OE simulator"

MOISE=`dirname $0`/..
LIB=$MOISE/lib

export CLASSPATH=$LIB/moise.jar:$CLASSPATH

#LIB=`dirname $0`
# add all .jar in ulib directory
#for j in `ls $LIB/*jar`; do
#    echo "  ." adding $j on classpath
#    CLASSPATH=$CLASSPATH:$j
#done

java -classpath $CLASSPATH moise.tools.SimOE $*
