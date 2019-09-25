package com;

import org.snmp4j.PDU;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class GenericEvent implements Delayed {

   public PDU pdu;

    public GenericEvent(PDU pdu) {
        this.pdu=pdu;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return 0;
    }

    @Override
    public int compareTo(Delayed o) {
        return 0;
    }
}
