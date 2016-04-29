#!/bin/sh

MOISE=`dirname $0`/..

java -classpath $MOISE/lib/moise.jar:$MOISE/lib/cartago.jar:$MOISE/lib/c4jason.jar:$MOISE/lib/jason.jar moise.tools.os2dot $*
