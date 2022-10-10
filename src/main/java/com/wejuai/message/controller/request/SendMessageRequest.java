package com.wejuai.message.controller.request;

import com.wejuai.entity.mongo.MediaType;

/**
 * @author ZM.Wang
 */
public class SendMessageRequest {

    /** userId */
    private String recipient;
    private String message;
    private String ossKey;
    private MediaType mediaType;

    public String getRecipient() {
        return recipient;
    }

    public String getMessage() {
        return message;
    }

    public String getOssKey() {
        return ossKey;
    }

    public MediaType getMediaType() {
        return mediaType;
    }
}
