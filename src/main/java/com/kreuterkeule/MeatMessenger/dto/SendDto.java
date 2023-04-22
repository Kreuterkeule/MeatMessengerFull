package com.kreuterkeule.MeatMessenger.dto;

public class SendDto {

    private String message;
    private String receiverToken;

    public SendDto(String message, String receiverToken) {
        this.message = message;
        this.receiverToken = receiverToken;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiverToken() {
        return receiverToken;
    }

    public void setReceiverToken(String receiverToken) {
        this.receiverToken = receiverToken;
    }
}
