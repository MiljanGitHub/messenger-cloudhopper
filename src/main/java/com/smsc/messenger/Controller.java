package com.smsc.messenger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.commons.util.windowing.WindowFuture;
import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppClient;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.*;
import com.cloudhopper.smpp.type.Address;
import net.sf.ehcache.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("cloudhopper")
@Slf4j
public class Controller {

    @Autowired private SchedulerConfiguration schedulerConfiguration;
    @Autowired private DefaultSmppClient defaultSmppClient;
    @Autowired private SmppSession smppSession;
    @Qualifier(SchedulerConfiguration.MESSENGER_MS_EXECUTOR)
    @Autowired private Executor executor;

    @GetMapping("/send/{messageCount}")
    public String sendAll(@PathVariable("messageCount") Integer messageCount){

        final List<SMSMessage> messages = getMessages(messageCount);
        final List<SMSMessage> threadSafeMessages = Collections.synchronizedList(messages);
        sendOldWay(threadSafeMessages);

        return "-1";
    }

    private void sendOldWay(List<SMSMessage> threadSafeMessages){
        List<List<SMSMessage>> messageList = new ArrayList<>(); //do some logic for partitioning of DTO messages to be sent

        //ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(schedulerConfiguration.getPoolSize(), new NamedThreadFactory("ParallelMMS"));

        for (List<SMSMessage> list : messageList){
            executor.execute(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                sendSmsList(list);
                            } catch (Exception e) {
                                log.error("Exception occurred while sending smsMessages",  e);
                            }
                        }
                    });
        }
        //executorService.shutdown();
    }

    private void sendSmsList(List<SMSMessage> messagesList) throws Exception {

        for (SMSMessage smsMessage : messagesList){
            try {

                byte[] textBytes = CharsetUtil.encode(smsMessage.getText(), CharsetUtil.CHARSET_GSM);

                SubmitSm submit0 = new SubmitSm();
                submit0.setSourceAddress(new Address((byte) 0x05, (byte) 0x01, smsMessage.getFrom()));
                submit0.setDestAddress(new Address((byte) 0x01, (byte) 0x01, smsMessage.getTo()));
                submit0.setShortMessage(textBytes);
                submit0.setDataCoding(SmppConstants.DATA_CODING_LATIN1);
                submit0.setEsmClass((byte) 0);
                submit0.setRegisteredDelivery((byte) 0);
                submit0.setScheduleDeliveryTime(null);

                log.info("About to invoke submitShortMessage to send... with id " + smsMessage.getId());
                SubmitSmResp submitResp = smppSession.submit(submit0, 10000);
                if (submitResp.getCommandStatus() == SmppConstants.STATUS_OK) {
                    // log.info("SMS submitted, message id {}", submitResp.getMessageId());
                    log.info("sendAll[submitShortMessage] SUCCESS Result {}. Where id {}", submitResp.getMessageId(), smsMessage.getId() );
                } else {
                    log.info("sendAll[submitShortMessage] FAIL Where id {}", smsMessage.getId() );

                    throw new IllegalStateException(submitResp.getResultMessage());
                }


            } catch (Exception e){
                log.info("Exception when sending msg...");
                e.printStackTrace();
            }

        }

    }

    private List<SMSMessage> getMessages(int messagesCount){

        return IntStream.rangeClosed(1, messagesCount).mapToObj(id -> SMSMessage.builder().id(id).from("0779868777").to("0779868777").text("demo text").build()).collect(Collectors.toList());
    }

}
