package com.smsc.messenger;

import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public  class ClientSmppSessionHandler extends DefaultSmppSessionHandler {



    public ClientSmppSessionHandler() {
        super(log);
    }

    @Override
    public void firePduRequestExpired(PduRequest pduRequest) {
        log.warn("PDU request expired: {}", pduRequest);
    }

    @Override
    public PduResponse firePduRequestReceived(PduRequest pduRequest) {
        PduResponse response = pduRequest.createResponse();

        // do any logic here

        return response;
    }

}
