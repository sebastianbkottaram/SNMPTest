package com;

import org.snmp4j.*;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.security.Priv3DES;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.smi.*;
import org.snmp4j.transport.AbstractTransportMapping;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;


public class GenericForwarding implements CommandResponder {


    public static void main(String[] args) {

        GenericForwarding snmp4jTrapReceiver = new GenericForwarding();
        GenericForwarding snmp4jTrapReceiver2 = new GenericForwarding();

        try{
           /* String[] credentials = getAllEMSCredentialsFromFile("/home/admin/mediation_bridges/nbitester/com.SNMPTest/config/SNMPConfig.txt");
            String ip = credentials[0];
            String port = credentials[1];*/
            System.out.println(new Date());
            snmp4jTrapReceiver.listen(new UdpAddress("192.168.11.237/8843"));
            snmp4jTrapReceiver2.listen(new UdpAddress("192.168.11.237/8845"));
            System.out.println("new value "+new Date());


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Trap Listner
     */



    public synchronized void listen(TransportIpAddress address)
            throws IOException {
        AbstractTransportMapping transport;

        if (address instanceof TcpAddress) {
            transport = new DefaultTcpTransportMapping((TcpAddress) address);
        } else {
            transport = new DefaultUdpTransportMapping((UdpAddress) address);
        }

        ThreadPool threadPool = ThreadPool.create("DispatcherPool", 10);
        MessageDispatcher mDispathcher = new MultiThreadedMessageDispatcher(
                threadPool, new MessageDispatcherImpl());

        // add message processing models
        mDispathcher.addMessageProcessingModel(new MPv1());
        mDispathcher.addMessageProcessingModel(new MPv2c());

        // add all security protocols
        SecurityProtocols.getInstance().addDefaultProtocols();
        SecurityProtocols.getInstance().addPrivacyProtocol(new Priv3DES());

        // Create Target
        // CommunityTarget target = new CommunityTarget();
        //  target.setCommunity(new OctetString("public"));

        Snmp snmp = new Snmp(mDispathcher, transport);
        snmp.addCommandResponder(this);

        transport.listen();
        System.out.println("Listening on " + address);


    }

    /**
     * This method will be called whenever a pdu is received on the given port
     * specified in the listen() method
     */
    public  void processPdu(CommandResponderEvent cmdRespEvent) {

        System.out.println("Received PDUEvent...");
        PDU pdu = cmdRespEvent.getPDU();
        try {
            if (pdu != null) {

        /*        System.out.println("Trap Type seb  = " + pdu.getType());
                System.out.println("Variables seb = " + pdu.getVariableBindings());
                System.out.printf(""+pdu.toString());*/

                 System.out.println("Trap Type seb  = " + pdu.getType());
                System.out.println("Variables seb = " + pdu.getVariableBindings());

            }
        }
        catch(Exception e)
        {


        }

    }


    protected static String[] getAllEMSCredentialsFromFile(String fileName) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));

        try {
            String readline;
            while ((readline = br.readLine()) != null) {
                if (readline.trim().startsWith("#") || readline.trim().length() == 0) {
                    continue;
                }

                String[] emsCredentials = readline.split(",");

                System.out.println("\nEMS Details : IP = " + emsCredentials[0].trim() + ", Port = " + emsCredentials[1].trim() + ", UserName = " + emsCredentials[2].trim());
                return emsCredentials;
            }
        } catch (Exception e) {

        } finally {
            br.close();
        }

        return null;
    }
}
