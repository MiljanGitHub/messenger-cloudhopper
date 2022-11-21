package com.smsc.messenger;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SMSMessage {
    private int id;
    private String to;
    private String from;
    private String text;

    public SMSMessage(int id, String to, String from, String text) {
        this.id = id;
        this.to = to;
        this.from = from;
        this.text = text;
    }
}