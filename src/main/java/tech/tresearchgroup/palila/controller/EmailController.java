package tech.tresearchgroup.palila.controller;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailController {
    public boolean sendEmail(String to, String subject, String body) {
        String from = "akash@gmail.com";
        String host = "localhost";
        int port = 465;
        final String username = "username@gmail.com";
        final String password = "mypassword";

        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        Session session = Session.getDefaultInstance(properties,
            new Authenticator() {
                protected PasswordAuthentication
                getPasswordAuthentication() {
                    return new PasswordAuthentication("username",
                        "password");
                }
            });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
            return true;
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
        return false;
    }
}
