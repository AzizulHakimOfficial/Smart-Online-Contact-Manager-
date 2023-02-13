package com.project.contact.service;

import java.util.Properties;

import org.springframework.stereotype.Service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
	
public boolean sendEmail(String subject,String text,String to) {
		
		boolean f=false;
		
		String from="-----@gmail.com";
		//variable for gmail
		
		//get the system properties
		Properties properties = new Properties();
		
		//setting important information to properties object
		
		//host set
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		
		String username="------";
		String password="---";
		
		//step 1: to get the session object
		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
			
				return new PasswordAuthentication(username,password);
			}
		});
		
		//step 2: compose the message[text,multi media]
		Message Smessage=new MimeMessage(session);
		
		try {
			//from email
			
			Smessage.setRecipient(Message.RecipientType.TO,new InternetAddress(to));
			Smessage.setFrom(new InternetAddress(from));
			Smessage.setSubject(subject);
			//Smessage.setText(text);
			Smessage.setContent(text, "text/html");
			//send
			
			//step-3: send the message using transport class
			Transport.send(Smessage);
			f=true;
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return f;
	}
}
