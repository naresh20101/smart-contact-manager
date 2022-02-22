package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

@Controller
public class ForgotController {
	@Autowired
	private EmailService emailService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@GetMapping("/forgot")
	public String openEmailForm()
	{
		return"forgot_email_form";
	}
	@PostMapping("/send-otp")
	public String sendOtp(@RequestParam("email") String email, HttpSession session)
	{
		System.out.println("email"+ email);
		Random random=new Random(1000);
		int otp=random.nextInt(99999999);
		System.out.println("otp::"+ otp);
		// Code for Send OTP to Email.
				String subject = "OTP Number from Smart Contact Manager";
				String message = ""
						+ "<div style='border:2px solid #e2e2e2; padding:20px'>"
						+ "<center><h1>"
						+ "Your OTP Number for Change Password is : "
						+ "<b>"+otp
						+ "</b>"
						+ "</h1></center>"
						+ "</br></br></br>"
						+ "<center><h1><b>------------ Thank you For Using Smart Contact Manager. ---------</b></center>"
						+ "</div>";
				String to = email;
				boolean flag = this.emailService.sendEmail(subject, message, to);
				if (flag) {
					session.setAttribute("myOtp", otp);
					session.setAttribute("email", email);
					return "verify_otp";
				} else {
					session.setAttribute("message", "Check Your Email ID !!");
					return "forgot_email_form";
				}
		
	}
	// Verify OTP
		@PostMapping("/verify-otp")
		public String verifOtp(@RequestParam("otp") int otp, HttpSession session)
		{
			int myOtp = (int) session.getAttribute("myOtp");
			String email = (String) session.getAttribute("email");
			if(myOtp==otp)
			{
				User user = this.userRepository.getUserByUserName(email);
				if(user==null)
				{
					session.setAttribute("message", "User does not exists with this Email !!");
					return "forgot_email_form";
				}
				else {
					
				}
				return "password_change_form";
			}
			else {
				session.setAttribute("message", "Please Enter Correct OTP Number...");
				return "verify_otp";
			}
		}
		@PostMapping("/change-password")
		public String changePassword(@RequestParam("newPassword") String newPassword, HttpSession session) {
			String email = (String) session.getAttribute("email");
			User user = this.userRepository.getUserByUserName(email);
			user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(user);
			return "redirect:/signin?change=Password Changed Successfully";
			
		}
	

}
