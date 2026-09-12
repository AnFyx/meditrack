package aiv.jms;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.Dependent;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Envoi des notifications par courriel.
 *
 * <p>La session de messagerie vient du serveur d'applications, via la ressource
 * JNDI {@code java:/mail/MyMail} : hôte, port et identifiants SMTP sont donc
 * configurés dans WildFly et n'apparaissent jamais dans le code ni dans le
 * dépôt. Voir la section « Configuration » du README.
 */
@Dependent
public class MailSender implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(MailSender.class.getName());

    @Resource(lookup = "java:/mail/MyMail")
    private Session mailSession;

    /**
     * Envoie un message. Un échec est journalisé sans interrompre le traitement
     * en cours : la notification est accessoire, l'affectation reste valide.
     *
     * <p>L'adresse du destinataire n'est pas journalisée : c'est une donnée
     * personnelle, et les journaux du serveur ne sont pas un endroit pour en
     * conserver.
     */
    public void send(String to, String subject, String body) {
        try {
            Message message = new MimeMessage(mailSession);
            message.setFrom();
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            LOGGER.fine("Notification sent");
        } catch (MessagingException e) {
            LOGGER.log(Level.WARNING, "Failed to send a notification", e);
        }
    }
}
