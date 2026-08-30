package com.app.hourbook.email;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

	@Autowired 
	private JavaMailSender jms;

	public String sendOtpEmail(String to, String otp) throws Exception {

		String subject = "OTP Verification";
		String body = "<h3>Your OTP is: <b>" + otp + "</b></h3>" + "<p>This OTP is valid for 5 minutes.</p>";

		MimeMessage message = jms.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);

		System.out.println("TO = " + to);

		helper.setFrom("OtpService31@gmail.com");
		helper.setTo(to);
		helper.setSubject(subject);
		helper.setText(body, true);

		System.out.println("Recipients = " + message.getAllRecipients()[0]);

		jms.send(message);

		return otp;
	}

	public String genrateOtp() { // otp genrator
		Random random = new Random();

		int otp = 100000 + random.nextInt(900000);
		return String.valueOf(otp);
	}

}
