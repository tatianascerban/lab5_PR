package com.xxxweb.emailclient.service.dto;

public class MailSendDTO {

    private String receiver;
    private String subject;
    private String content;

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "MailSendDTO{" + "receiver='" + receiver + '\'' + ", subject='" + subject + '\'' + ", content='" + content + '\'' + '}';
    }
}
