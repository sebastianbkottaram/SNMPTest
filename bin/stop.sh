cd ..

MED_PID=`cat ms.pid`
MED_SERVER_HOME=`pwd`
echo Stopping SNMP  Server with PID $MED_PID
kill  $MED_PID
rm $MED_SERVER_HOME/ms.pid

