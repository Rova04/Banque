
package Model;

//import java.util.Properties;
//import jakarta.mail.Message;
//import jakarta.mail.MessagingException;
//import jakarta.mail.PasswordAuthentication;
//import jakarta.mail.Session;
//import jakarta.mail.Transport;
//import jakarta.mail.internet.InternetAddress;
//import jakarta.mail.internet.MimeMessage;
//
//public class Mail {
//
//    public static void main(String[] args) {
//        // Paramètres du serveur SMTP et informations d'authentification
//        String host = "smtp.gmail.com"; // ex: smtp.gmail.com
//        String username = "rovarafidy@gmail.com";
//        String password = "kmod bmkk icni bvpn";
//        String port = "587"; // Le port peut varier selon le serveur SMTP
//
//        // Informations du destinataire et message
//        String to = "rovanagarcia@gamil.com";
//        String subject = "Sujet de l'email";
//        String body = "Bonjour, ceci est un e-mail envoyé depuis Java!";
//
//        try {
//            // Configurer les propriétés du serveur SMTP
//            Properties props = configureMailProperties(host, port);
//
//            // Créer une session avec authentification
//            Session session = createMailSession(props, username, password);
//
//            // Créer le message
//            Message message = createEmailMessage(session, username, to, subject, body);
//
//            // Envoyer l'e-mail
//            sendEmail(message);
//
//            System.out.println("L'e-mail a été envoyé avec succès.");
//        } catch (MessagingException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static Properties configureMailProperties(String host, String port) {
//        Properties props = new Properties();
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.smtp.host", host);
//        props.put("mail.smtp.port", port);
//
//        // Propriétés supplémentaires pour la sécurité TLS
//        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.1 TLSv1");
//        props.put("mail.smtp.ssl.ciphersuites", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
//
//        return props;
//    }
//
//    private static Session createMailSession(Properties props, final String username, final String password) {
//        return Session.getInstance(props, new jakarta.mail.Authenticator() {
//            protected PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication(username, password);
//            }
//        });
//    }
//
//    private static Message createEmailMessage(Session session, String from, String to, String subject, String body) throws MessagingException {
//        Message message = new MimeMessage(session);
//        message.setFrom(new InternetAddress(from));
//        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
//        message.setSubject(subject);
//        message.setText(body);
//        return message;
//    }
//
//    private static void sendEmail(Message message) throws MessagingException {
//        Transport.send(message);
//    }
//}
//
//




import java.util.Properties;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Mail {
    
   private static final String HOST = "smtp.gmail.com";
    private static final String PORT = "587";
    private static final String USERNAME = "rovarafidy@gmail.com";
    private static final String PASSWORD = "kmod bmkk icni bvpn";
    private static final String SENDER_NAME = "Banque Tosika";


    public static void main(String[] args) throws UnsupportedEncodingException, MessagingException {
        String to = "rovanagarcia@gmail.com"; // Adresse e-mail du destinataire
        String clientName = "Nom Client"; // Nom du client à récupérer dynamiquement
        String montantPrete = "1000"; // Montant du prêt à récupérer dynamiquement

        // Calculer la date limite de remboursement (200 jours après aujourd'hui)
        LocalDate dueDate = LocalDate.now().plusDays(200);
        String dueDateString = dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        // Envoyer une notification
        sendNotification(to, clientName, montantPrete, dueDateString);
    }

public static void sendNotification(String to, String clientName, String montantRembourse, String dueDate) throws MessagingException, UnsupportedEncodingException {
        // Configurer les propriétés du serveur SMTP
        Properties props = configureMailProperties();

        // Créer une session avec authentification
        Session session = createMailSession(props);

        // Créer le message
        String subject = "Notification de Prêt";
        String body = "Cher " + clientName + ",\n\n"
                    + "Votre prêt auprès de la " + SENDER_NAME + " a bien été effectué. "
                    + "Vous devriez rembourser le montant de " + montantRembourse + "Ar au plus tard le " + dueDate + ".\n\n"
                    + "Cordialement,\n"
                    + SENDER_NAME;

        Message message = createEmailMessage(session, USERNAME, to, subject, body);

        // Envoyer l'e-mail
        sendEmail(message);
    }

    private static Properties configureMailProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        return props;
    }

    private static Session createMailSession(Properties props) {
        return Session.getInstance(props, new jakarta.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });
    }

    private static Message createEmailMessage(Session session, String from, String to, String subject, String body) throws MessagingException, UnsupportedEncodingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from, SENDER_NAME));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);
        return message;
    }

    private static void sendEmail(Message message) throws MessagingException {
        Transport.send(message);
        System.out.println("Envoie email avec succes");
    }
}
