package com.xxxweb.emailclient.resource;


import com.xxxweb.emailclient.service.MailService;
import com.xxxweb.emailclient.service.dto.MailDTO;
import com.xxxweb.emailclient.service.dto.MailSendAttachmentDTO;
import com.xxxweb.emailclient.service.dto.MailSendDTO;
import java.io.IOException;
import java.util.List;
import javax.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/api/mail")
@RestController
public class MailResource {

  private final MailService mailService;

  public MailResource(MailService mailService) {
    this.mailService = mailService;
  }

  @GetMapping("/imaps")
  public ResponseEntity<List<MailDTO>> getImapsMails(@RequestParam("email") String email, @RequestParam("password") String password)
      throws MessagingException {
    var emails = mailService.readInboxImap(email, password);
    return ResponseEntity.ok(emails);
  }

  @GetMapping("/pop3")
  public ResponseEntity<List<MailDTO>> getPop3Mails(@RequestParam("email") String email, @RequestParam("password") String password)
      throws MessagingException {
    var emails = mailService.readInboxPop(email, password);
    return ResponseEntity.ok(emails);
  }

  @GetMapping("/imaps/attachment")
  public ResponseEntity<byte[]> getPop3Attachment(
      @RequestParam("email") String email,
      @RequestParam("password") String password,
      @RequestParam("messageNr") int messageNr
  ) throws IOException, MessagingException {
    var emails = mailService.downloadAttachment(email, password, messageNr);
    return ResponseEntity.ok(emails);
  }

  @PostMapping("/smtp")
  public ResponseEntity<Void> sendMessage(
      @RequestParam("email") String email,
      @RequestParam("password") String password,
      @RequestBody MailSendDTO mailSendDTO
  ) throws MessagingException {
    mailService.sendMessage(email, password, mailSendDTO);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/smtp/attachment")
  public ResponseEntity<Void> sendMessage(
      @RequestParam("email") String email,
      @RequestParam("password") String password,
      @RequestBody MailSendAttachmentDTO mailSendDTO
  ) throws IOException, MessagingException {
    mailService.sendMessage(email, password, mailSendDTO);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/smtp/reply")
  public ResponseEntity<Void> sendMessage(
      @RequestParam("email") String email,
      @RequestParam("password") String password,
      @RequestParam("reply") String reply,
      @RequestBody MailSendAttachmentDTO mailSendDTO
  ) throws IOException, MessagingException {
    mailService.sendMessage(email, password, reply, mailSendDTO);
    return ResponseEntity.noContent().build();
  }
}
