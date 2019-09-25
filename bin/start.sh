#!/bin/sh

cd ..
#CLASSPATH=
#export CLASSPATH

#CONF='conf'
#export CONF

MP_HOME=`pwd`
JAVA_HOME=$MP_HOME/jre

CLASS_PATH=classes/*:lib/*:conf/

export JAVA_HOME MP_HOME CLASS_PATH

rm -rvf _nsdb_root

#**********IMPORTANT NOTES*******************
# Pl set JMX_PORT
# Based on application heap requirements, Pl set JVM_HEAP_OPTS
#**************************************

#Configure JMX port here
JMX_PORT=9996
JAVA="$JAVA_HOME/bin/java"
#JACORB_OPTS="-Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton"
JMX_OPTS="-Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=$JMX_PORT"
JVM_GC_LOG_OPTS="-Xloggc:$MP_HOME/jvm.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintTenuringDistribution"
#Remove -XX:+UseCompressedOops for 32 bit jvm 
JVM_PERFORMANCE_OPTS="-server -XX:+UseCompressedOops -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:+DisableExplicitGC -XX:+CMSParallelRemarkEnabled -Djava.awt.headless=true"
COMMON_OPTS="-Dnet.sf.ehcache.enableShutdownHook=true -Dmultiplug.home=$MP_HOME -DCONF_PATH=$CONF"
RMI_OPTS="-Djava.rmi.server.codebase="file://$MP_HOME/classes/cygnet-ms-base.jar""

#Default Memory Size, add/modify -Xmx,-Xms flags based on application needs. Sample JVM_HEAP_OPTS given below
JVM_HEAP_OPTS="-XX:MaxPermSize=256m"

LOG4J="-Dlog4j.configurationFile=conf/log4j2.xml"

#2G heap
#JVM_HEAP_OPTS="-Xms2g -Xmx2g -XX:NewSize=512m -XX:MaxNewSize=512m -XX:MaxPermSize=256m"

#50GB heap
#JVM_HEAP_OPTS="-Xms50g -Xmx50g -XX:MaxPermSize=256m"

# Add below props based on requirements
# -Djacorb.iiop.alternate_addresses=10.5.185.29 to configure alternate address for NAT/Firewall senarios
# -Djacorb.connection.client.pending_reply_timeout=54000000 to set client connection timeout

nohup $JAVA $JVM_HEAP_OPTS $LOG4J $JVM_PERFORMANCE_OPTS $JVM_GC_LOG_OPTS $JMX_OPTS $JACORB_OPTS $COMMON_OPTS -cp $CLASS_PATH $RMI_OPTS com.SNMPTest&


#Enable this line for IBM Java. comment out above line.
#nohup $JAVA -d64 -Xmn1g -Xms3g -Xmx3g -XX:MaxPermSize=256m -Xgcpolicy:gencon -Xverbosegclog:$MP_HOME/gc.log $JMX_OPTS $JACORB_OPTS $COMMON_OPTS -cp $CLASS_PATH $RMI_OPTS in.co.nmsworks.ms.system.Controller&

MED_PID=$!
echo SNMP Server PID = $MED_PID
echo $MED_PID>ms.pid
err=$?
if [ "$err" -ne "0" ]
then
	echo -----FATAL-----
	echo Unable to write Mediation Server PID file
	echo in $MP_HOME. Check permissions or disk space.
	kill $MED_PID
	exit 1
fi
echo Check $MP_HOME/nohup.out for startup logs.
echo Use stop.sh to stop server.
