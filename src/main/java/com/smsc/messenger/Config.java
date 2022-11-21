package com.smsc.messenger;

import com.cloudhopper.commons.util.windowing.WindowFuture;
import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppClient;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.EnquireLink;
import com.cloudhopper.smpp.pdu.EnquireLinkResp;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.util.NamedThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@Slf4j
public class Config {

    public static final String MESSENGER_MS_EXECUTOR = "MessengerMsExecutor";


    @Autowired private Executor executor;

    @Value("${messenger.smsc.window}")
    private int window;

    @Value("${messenger.smsc.sessions}")
    private int sessions;

    @Value("${messenger.smsc.host}")
    private String host;

    @Value("${messenger.smsc.port}")
    private int port;

    @Value("${messenger.smsc.systemId}")
    private String systemId;

    @Value("${messenger.smsc.password}")
    private String password;

    @Bean
    public SmppSession getSmppSession(){

        SmppSessionConfiguration config0 = new SmppSessionConfiguration();
        config0.setWindowSize(window);
        config0.setName("Tester.Session.0");
        config0.setType(SmppBindType.TRANSCEIVER);
        config0.setSystemType(null);
        config0.setHost(host);
        config0.setPort(port);
        config0.setConnectTimeout(10000);
        config0.setSystemId(systemId);
        config0.setPassword(password);
        config0.getLoggingOptions().setLogBytes(false);
        config0.getLoggingOptions().setLogPdu(true);
        // to enable monitoring (request expiration)
        config0.setRequestExpiryTimeout(30000);
        config0.setWindowMonitorInterval(15000);
        config0.setCountersEnabled(true);



        DefaultSmppSessionHandler sessionHandler = new ClientSmppSessionHandler();
        SmppSession session0 = null;
        try {
            session0 = getDefaultSmppClient().bind(config0, sessionHandler);
            EnquireLinkResp enquireLinkResp1 = session0.enquireLink(new EnquireLink(), 10000);
            log.info("enquire_link_resp #1: commandStatus [" + enquireLinkResp1.getCommandStatus() + "=" + enquireLinkResp1.getResultMessage() + "]");

            //Attempted sending EnquireLink using both ways...
            /*WindowFuture<Integer, PduRequest, PduResponse> future0 = session0.sendRequestPdu(new EnquireLink(), 10000, true);
            if (!future0.await()) {
                log.error("Failed to receive enquire_link_resp within specified time");
            } else if (future0.isSuccess()) {
                EnquireLinkResp enquireLinkResp2 = (EnquireLinkResp) future0.getResponse();
                log.info("enquire_link_resp #2: commandStatus [" + enquireLinkResp2.getCommandStatus() + "=" + enquireLinkResp2.getResultMessage() + "]");
            } else {
                log.error("Failed to properly receive enquire_link_resp: " + future0.getCause());
            }*/

        } catch (Exception e){
            log.info("Error in Config...");
            e.printStackTrace();
        }

        return session0;
    }

    @Bean
    public DefaultSmppClient getDefaultSmppClient(){

        //Attempted this as well...
        //ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(schedulerConfiguration.getPoolSize(), new NamedThreadFactory("ParallelSMS"));

        DefaultSmppClient clientBootstrap = new DefaultSmppClient(Executors.newCachedThreadPool(), sessions);

        return clientBootstrap;

    }


}
