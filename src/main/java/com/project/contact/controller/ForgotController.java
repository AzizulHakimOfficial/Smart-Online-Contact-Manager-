package com.project.contact.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.contact.dao.UserRepository;
import com.project.contact.entities.User;
import com.project.contact.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	
	Random random=new Random(1000);
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private EmailService emailService;
	//email id form open handler
	@GetMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,HttpSession session) {
		
		//generating random otp

		int otp = random.nextInt(9999);
		
		String subject="OTP from SCM";
		String message = ""
				+ "<div style='border:1px solid #e2e2e2; padding:20px'>"
				+ "<h3>"
				+ "OTP is "
				+ "<b>"+otp
				+ "</n>"
				+ "</h3>"
				+ "</div>";
		String to=email;
		boolean flag = this.emailService.sendEmail(subject, message, to);
		if(flag) {
			
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
		}else {
			session.setAttribute("message", "Check your email..");
			return "forgot_email_form";
		}
	}
	
	@PostMapping("/verify-otp")
	public String verifyOTP(@RequestParam("otp") int otp,HttpSession session){
		int myotp = (int)session.getAttribute("myotp");
		String email= (String) session.getAttribute("email");
		if(myotp==otp) {
			User user = this.userRepository.getUserByUserName(email);
			if(user==null) {
				session.setAttribute("message", "User does not exist with this email.");
				return "forgot_email_form";
			}else {
				return "password_change_form";
			}
		}else {
			session.setAttribute("message", "Wrong OTP!!");
			return "verify_otp";
		}
	}
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword,HttpSession session) {
		String email= (String) session.getAttribute("email");
		User user= this.userRepository.getUserByUserName(email);
		user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
		this.userRepository.save(user);
		session.setAttribute("message", "password Changed.Login now!");
		return "redirect:/signin?change=password changed successfully..";
	}
	
	
}
