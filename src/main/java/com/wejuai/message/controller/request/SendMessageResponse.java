package com.wejuai.message.controller.request;

import com.wejuai.entity.mongo.MediaType;
import com.wejuai.entity.mongo.SendMessage;

import java.util.Date;

/**
 * @author ZM.Wang
 */
public class SendMessageResponse {

    private String id;
    private Date createdAt;

    private String sender;
    private String recipient;
    private String text;
    private MediaType mediaType;
    private String ossKey;

    private int code;

    private String error;

    public SendMessageResponse(SendMessage sendMessage) {
        this.id = sendMessage.getId();
        this.createdAt = sendMessage.getCreatedAt();
        this.sender = sendMessage.getSender();
        this.recipient = sendMessage.getRecipient();
        this.text = sendMessage.getText();
        this.mediaType = sendMessage.getMediaType();
        this.ossKey = sendMessage.getOssKey();
        this.code = 200;
        this.error = "";
    }

    public SendMessageResponse(int code, String error) {
        this.code = code;
        this.error = error;
    }

    SendMessageResponse(){
    }

    public String getId() {
        return id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getText() {
        return text;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public String getOssKey() {
        return ossKey;
    }

    public int getCode() {
        return code;
    }

    public String getError() {
        return error;
    }
}
