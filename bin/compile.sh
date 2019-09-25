#!/bin/sh
cd ..

JAVA_HOME=/usr/lib/jvm/jdk1.8.0_121/
#CLASS_PATH=lib/a-cygnet-tmf-enhanced-2015-08-14-16-15.jar:lib/*:conf/
CLASS_PATH=lib/snmp4j-2.5.0.jar:lib/AdventNetSnmp.jar

export JAVA_HOME CLASS_PATH

rm -rvf _nsdb_root

$JAVA_HOME/bin/javac -classpath $CLASS_PATH -d . `find src/ -name '*.java'`

if [ $? -ne 0 ] ; then
echo "### Compilation Error###"
exit
fi

zip -pr classes/SNMPServer.jar com/
rm -rf com
