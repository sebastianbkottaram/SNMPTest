package com;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.adventnet.snmp.beans.SnmpTable;
import com.adventnet.snmp.snmp2.*;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.*;


public class SNMPTest {

    public static final String community = "public";

    private static final String trapOid = ".1.3.6.1.2.1.1.8";
    private static final String trapOid2 = ".1.3.6.1.2.1.1.13";

    private static final String notifCounterOid = ".1.3.555.1.2.1.1.6";

    private static final int port = 162;
   // private static final int port = 161;
    static AtomicInteger counter = new AtomicInteger(0);

    //IP of Local Host
    public static final String ipAddress = "192.168.11.237";
    private static Timer timer;

    //Ideally Port 162 should be used to send receive Trap, any other available Port can be used

    public static void main(String[] args) {

        if(timer==null){
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                sendV1orV2Trap(SnmpConstants.version1,community,ipAddress,port,"Normal SNMP Traps",trapOid);
                 //   sendV1orV2Trap(SnmpConstants.version1,community,ipAddress,port,"SNMP Traps",trapOid);
                 //   sendV1orV2Trap(SnmpConstants.version1,community,ipAddress,port2,"Forward SNMP Traps",trapOid2);
                }
            };
            timer = new Timer();
            timer.schedule(timerTask,0, 20*1000);
        }
    }


    public static List<List<String>> getTableAsStrings(Snmp snmp,UserTarget communityTarget,OID[] oids) {
        TableUtils tUtils = new TableUtils(snmp, new DefaultPDUFactory());

        @SuppressWarnings("unchecked")
        List<TableEvent> events = tUtils.getTable(communityTarget, oids, null, null);

        List<List<String>> list = new ArrayList<List<String>>();
        for (TableEvent event : events) {
            if(event.isError()) {
               continue;
            }
            List<String> strList = new ArrayList<String>();
            list.add(strList);
            for(VariableBinding vb: event.getColumns()) {
                strList.add(vb.getVariable().toString());
                System.out.println("value :"+vb.getVariable().toString());
            }
        }
        return list;
    }


    private static void sendV1orV2Trap(int snmpVersion, String community,
                                       String ipAddress, int port, String octetString,String trapOid) {
        try {
            // create v1/v2 PDUEvent

           PDU snmpPDU = createPdu(snmpVersion,octetString,trapOid);

            // Create Transport Mapping
            TransportMapping<?> transport = new DefaultUdpTransportMapping();
            transport.listen();

            // Create Target
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString("public"));
            target.setAddress(GenericAddress.parse("192.168.11.237/162")); // supply your own IP and port
            target.setRetries(2);
            target.setTimeout(1500);
            target.setVersion(SnmpConstants.version1);


         /*   Map<String, String> result = doWalk(".1.3.6.1.4.1.637.65.1.1.1.2", target); // ifTable, mib-2 interfaces

            for (Map.Entry<String, String> entry : result.entrySet()) {
                if (entry.getKey().startsWith(".1.3.6.1.2.1.2.2.1.2.")) {
                    System.out.println("ifDescr" + entry.getKey().replace(".1.3.6.1.2.1.2.2.1.2", "") + ": " + entry.getValue());
                }
                if (entry.getKey().startsWith(".1.3.6.1.2.1.2.2.1.3.")) {
                    System.out.println("ifType" + entry.getKey().replace(".1.3.6.1.2.1.2.2.1.3", "") + ": " + entry.getValue());
                }

            }*/


            // Send the PDUEvent
           Snmp snmp = new Snmp(transport);

            snmp.listen();
        //    List<List<String>> al = getTableAsStrings(snmp,comtarget,new OID[]{new OID(".1.3.6.1.4.1.637.65.1.1.1.2")});*/

           System.out.println(snmpPDU.getVariableBindings());
           snmp.send(snmpPDU, target);


            /*System.out.println("Sent Trap to (IP:Port)=> " + ipAddress + ":"
                    + port+"SNMP Trap"+snmpPDU.getVariableBindings());*/


          snmp.close();
        } catch (Exception e) {
            System.err.println("Error in Sending Trap to (IP:Port)=> "
                    + ipAddress + ":" + port);
            System.err.println("Exception Message = " + e.getMessage());
        }
    }


    public static Map<String, String> doWalk(String tableOid, Target target) throws IOException {
        Map<String, String> result = new TreeMap<>();
        TransportMapping<? extends Address> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        OID[] sd = new OID[1];
        sd[0] = new OID(tableOid);

        TableUtils treeUtils = new TableUtils(snmp, new DefaultPDUFactory());
        List<TableEvent> events = treeUtils.getTable(target, sd,null,null);
        if (events == null || events.size() == 0) {
            System.out.println("Error: Unable to read table...");
            return result;
        }

        for (TableEvent event : events) {
            if (event == null) {
                continue;
            }
            if (event.isError()) {
                System.out.println("Error: table OID [" + tableOid + "] " + event.getErrorMessage());
                continue;
            }

            VariableBinding[] varBindings = event.getColumns();
            if (varBindings == null || varBindings.length == 0) {
                continue;
            }
            for (VariableBinding varBinding : varBindings) {
                if (varBinding == null) {
                    continue;
                }

                result.put("." + varBinding.getOid().toString(), varBinding.getVariable().toString());
            }

        }
        snmp.close();

        return result;
    }


    private static PDU createPdu(int snmpVersion,String message,String oid) {
        PDU pdu = DefaultPDUFactory.createPDU(snmpVersion);
        if (snmpVersion == SnmpConstants.version1) {
            pdu.setType(PDU.V1TRAP);
        } else {
            pdu.setType(PDU.TRAP);
        }
        pdu.add(new VariableBinding(SnmpConstants.sysUpTime,new OctetString(new Date().toString())));
        pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(oid)));
        pdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress,
                new IpAddress(ipAddress)));
        pdu.add(new VariableBinding(new OID(oid), new OctetString(message)));
        pdu.add(new VariableBinding(new OID(notifCounterOid), new OctetString("Counter="+counter.getAndDecrement())));
        return pdu;
    }



}

