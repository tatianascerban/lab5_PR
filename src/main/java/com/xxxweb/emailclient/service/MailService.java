package com.xxxweb.emailclient.service;

import com.xxxweb.emailclient.service.dto.MailDTO;
import com.xxxweb.emailclient.service.dto.MailSendAttachmentDTO;
import com.xxxweb.emailclient.service.dto.MailSendDTO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Store;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    public List<MailDTO> readInboxImap(String email, String password) throws MessagingException {
        List<MailDTO> viewDtoList = new ArrayList<>();
        Properties seshProperties = new Properties();
        seshProperties.put("mail.store.protocol", "imaps");
        Session session = Session.getDefaultInstance(seshProperties, null);
        Store store = session.getStore("imaps");
        store.connect("imap.gmail.com", email, password);
        Folder inbox = store.getFolder("Inbox");
        inbox.open(Folder.READ_ONLY);
        Arrays
            .stream(inbox.getMessages())
            .skip(Math.max(0, inbox.getMessages().length - 5))
            .forEach(message -> {
                try {
                    viewDtoList.add(new MailDTO(Arrays.toString(message.getFrom()), message.getSubject()));
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            });
        inbox.close(false);
        store.close();
        return viewDtoList;
    }

    public List<MailDTO> readInboxPop(String email, String password) throws MessagingException {
        List<MailDTO> viewDtoList = new ArrayList<>();
        var seshProperties = getPropertiesPop3();

        Session session = Session.getDefaultInstance(seshProperties, null);
        Store store = session.getStore("pop3");
        store.connect(email, password);
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
        Message[] messages = inbox.getMessages();
        for (var message : messages) {
            viewDtoList.add(new MailDTO(message.getFrom()[0].toString(), message.getSubject()));
        }
        inbox.close(false);
        store.close();
        return viewDtoList;
    }

    public byte[] downloadAttachment(String email, String password, int messageNr) throws MessagingException, IOException {
        Properties seshProperties = new Properties();
        seshProperties.put("mail.store.protocol", "imaps");
        Session session = Session.getDefaultInstance(seshProperties, null);
        Store store = session.getStore("imaps");
        store.connect("imap.gmail.com", email, password);
        Folder inbox = store.getFolder("Inbox");
        inbox.open(Folder.READ_ONLY);
        var message = inbox.getMessage(messageNr);
        if (message.getContentType().contains("multipart")) {
            Multipart multipart = (Multipart) message.getContent();

            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                    var content = bodyPart.getInputStream().readAllBytes();
                    inbox.close();
                    store.close();
                    return content;
                }
            }
        }
        inbox.close();
        store.close();
        throw new RuntimeException("No attachment");
    }

    private static Properties getPropertiesPop3() {
        Properties properties = new Properties();

        properties.setProperty("mail.store.protocol", "pop3");
        properties.put("mail.pop3s.port", "995" );
        properties.put("mail.pop3s.host", "pop.gmail.com");
        properties.setProperty("mail.pop3s.auth", "true");
        properties.setProperty("mail.pop3s.socketFactory.class",
            "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.pop3s.ssl.trust", "*");
        return properties;
    }

    public void sendMessage(String email, String password, MailSendDTO dto) throws MessagingException {
        Properties seshProperties = getPropertiesSmtp();
        Session session = getSession(email, password, seshProperties);
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(email));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(dto.getReceiver()));
        message.setSubject(dto.getSubject());
        message.setText(dto.getContent());
        Transport.send(message);
    }

    private static Session getSession(String email, String password, Properties seshProperties) {
        return Session.getInstance(
            seshProperties,
            new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(email, password);
                }
            }
        );
    }

    private static Properties getPropertiesSmtp() {
        Properties seshProperties = new Properties();
        seshProperties.put("mail.smtp.host", "smtp.gmail.com");
        seshProperties.put("mail.smtp.port", "465");
        seshProperties.put("mail.smtp.ssl.enable", "true");
        seshProperties.put("mail.smtp.auth", "true");
        return seshProperties;
    }

    public void sendMessage(String email, String password, MailSendAttachmentDTO dto) throws MessagingException, IOException {
        Properties seshProperties = getPropertiesSmtp();
        Session session = getSession(email, password, seshProperties);
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(email));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(dto.getReceiver()));
        message.setSubject(dto.getSubject());

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(dto.getContentText());

        MimeBodyPart attachmentBodyPart = new MimeBodyPart();
        var outputFile = new File(dto.getFilename());
        try (var outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(dto.getFileContent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        attachmentBodyPart.attachFile(outputFile);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        multipart.addBodyPart(attachmentBodyPart);

        message.setContent(multipart);
        Transport.send(message);
        outputFile.delete();
    }

    public void sendMessage(String email, String password, String reply, MailSendAttachmentDTO dto) throws MessagingException, IOException {
        Properties seshProperties = getPropertiesSmtp();
        Session session = getSession(email, password, seshProperties);
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(email));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(dto.getReceiver()));
        Address[] replyToAddresses = { new InternetAddress(reply) };
        message.setReplyTo(replyToAddresses);

        message.setSubject(dto.getSubject());

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(dto.getContentText());

        MimeBodyPart attachmentBodyPart = new MimeBodyPart();
        var outputFile = new File(dto.getFilename());
        try (var outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(dto.getFileContent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        attachmentBodyPart.attachFile(outputFile);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        multipart.addBodyPart(attachmentBodyPart);

        message.setContent(multipart);
        Transport.send(message);
        outputFile.delete();
    }
}
