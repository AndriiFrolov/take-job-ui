package org.example.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.example.dto.ConfigurationDto;
import org.example.dto.Job;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailService {

    public void sendEmail(ConfigurationDto configurationDto, Job job) throws MessagingException {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "smtp.elasticemail.com");
        prop.put("mail.smtp.port", "2525");

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("andriiua1989@outlook.com", "81C1B252D8D7A66ED7AAC5EE6F5D584483A5");
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("andriiua1989@outlook.com"));
        message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(configurationDto.getEmailToSend()));
        message.setSubject("New Job taken " + job.getPrice());

        String msg = "Customer: " + job.getCustomer() + "\n" +
                "Title: " + job.getTitle() + "\n" +
                "Price: " + job.getPrice() + "\n" +
                "Due: " + job.getDueDate() + "\n";

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(msg, "text/html; charset=utf-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setContent(multipart);

        Transport.send(message);
    }
}
