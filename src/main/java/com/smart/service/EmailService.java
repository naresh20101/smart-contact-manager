package com.smart.service;

import org.springframework.stereotype.Service;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Service
public class EmailService {
	public boolean sendEmail(String subject, String message, String to) {
		boolean f = false;
		String from = "khatrinaresh20101@gmail.com";

		String host = "smtp.gmail.com";

		Properties properties = System.getProperties();
		System.out.println("Properties : " + properties);
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");

		Session session = Session.getInstance(properties, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("khatrinaresh20101@gmail.com", "coder18CS47");
			}
		});
		session.setDebug(true);
		// Step 2 : compose the message [text,multimedia]

		MimeMessage m = new MimeMessage(session);
		try {
			// From Email
			m.setFrom(from);

			// Adding recipient to message

			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			// Adding Message to Subject

			m.setSubject(subject);

			// Adding Text to Message

			//m.setText(message);
			m.setContent(message,"text/html");

			// Step 3 : send the message using Transport Class
			Transport.send(m);
			System.out.println("Successfully Sent...........");
			f = true;
		} catch (Exception e) {
			System.out.println("Error : " + e.getMessage());
			e.printStackTrace();
		}
		return f;
	}
	

}
