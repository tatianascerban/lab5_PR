package com.xxxweb.emailclient.service.dto;

import java.util.Arrays;

public class MailSendAttachmentDTO {

    private String receiver;
    private String subject;
    private String contentText;
    private byte[] fileContent;

    private String filename;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

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

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    @Override
    public String toString() {
        return (
            "MailSendAttachmentDTO{" +
            "receiver='" +
            receiver +
            '\'' +
            ", subject='" +
            subject +
            '\'' +
            ", contentText='" +
            contentText +
            '\'' +
            ", fileContent=" +
            Arrays.toString(fileContent) +
            '}'
        );
    }
}
