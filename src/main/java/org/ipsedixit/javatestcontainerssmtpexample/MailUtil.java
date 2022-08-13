package org.ipsedixit.javatestcontainerssmtpexample;

import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

public class MailUtil {

	private static final Logger LOGGER = LogManager.getLogger();

	private MailUtil() {
	}

	/**
	 * This a very pretty example of sending an email and there are some
	 * simplification. For example in sending an email could be more parameters
	 * (such as CopyCarbon), HTML and plain text, ...
	 * 
	 * @throws MessagingException
	 *             An error occurred in sending email.
	 */
	public static void send(String host, int port, String fromAddr, List<String> toAddrs, String subject,
			String msgBody) throws MessagingException {
		try {
			Properties prop = new Properties();
			prop.put("mail.smtp.auth", false);
			prop.put("mail.smtp.starttls.enable", "true");
			prop.put("mail.smtp.host", host);
			prop.put("mail.smtp.port", port);
			LOGGER.info("Try open connection to host: {} - port: {}", host, port);
			Session session = Session.getInstance(prop);
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(fromAddr));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(String.join(",", toAddrs)));

			message.setSubject(subject);

			Multipart multipart = new MimeMultipart();

			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			mimeBodyPart.setContent(msgBody, "text/html");
			multipart.addBodyPart(mimeBodyPart);

			message.setContent(multipart);
			LOGGER.info("Send mail from_addr: {} - to_addrs: {} - msg: {}", fromAddr, toAddrs, msgBody);
			Transport.send(message);

		} catch (MessagingException e) {
			LOGGER.error("Error sending mail", e);
			throw e;
		}
	}

}
