package aiv.jms;

import jakarta.enterprise.context.Dependent;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.Serializable;
import java.util.Properties;

@Dependent
public class MailSender implements Serializable {

    public static void send(String to, String subject, String body) {
        final String username = "tomin83210@gmail.com";
        final String password = "smyk qdif siky asai";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

            System.out.println("✅ Email sent to " + to);
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Email sent to " + to));

        } catch (MessagingException e) {
            System.out.println("❌ Failed to send email to " + to);
            e.printStackTrace();
        }
    }
}
