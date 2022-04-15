package id.gultom.warta.service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.util.Properties;

public class EmailService {

    private Session session;

    public EmailService() throws IOException {
        try (InputStream output = new FileInputStream("config.properties")) {
            Properties props = new Properties();
            props.load(output);
            session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                            props.getProperty("mail.smtp.user"),
                            props.getProperty("mail.smtp.password")
                    );
                }
            });
        }
    }

    public EmailService(Session session) {
        this.session = session;
    }

    public void send(String subject, String body, String emails) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(session.getProperty("mail.smtp.user")));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emails));
        message.setSubject(subject);
        message.setContent(body, "text/html; charset=UTF-8");
        System.out.println(String.format("Sending email to %s...", emails));
        Transport.send(message);
    }

    public void sendWithAttachment(
            String attachmentPath,
            String subject,
            String body,
            String emails) throws IOException, MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(session.getProperty("mail.smtp.user")));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emails));
        message.setSubject(subject);
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(body, "text/html; charset=UTF-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.attachFile(new File(attachmentPath));
        multipart.addBodyPart(attachmentPart);

        message.setContent(multipart);
        System.out.println(String.format("Sending email to %s...", emails));
        Transport.send(message);
    }

}
