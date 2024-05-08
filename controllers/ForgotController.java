package com.smart.controllers;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.sessionHelper;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	Random random = new Random(1000);
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private BCryptPasswordEncoder bcrypt;
	
	
	//email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}
	
	
	@PostMapping("/send-OTP")
	public String sendOTP(@RequestParam("email") String email, HttpSession session){
		System.out.println("Email "+email);
		
		//Generating otp of 4 digits
		
		int otp=random.nextInt(999999);
		System.out.println("OTP "+ otp);
		
		//to send OTP to the given email
		
		/*
		 * String subject="OTP for changing your password of Smart conatct Manager";
		 * String message="<h1> OTP = "+otp+"<h1>"; String to= email; boolean
		 * flag=this.emailService.sendEmail(email, email, email);
		 */
		
		 // To send OTP to the given email
	    String subject = "OTP for changing your password of Smart contact Manager";
	    String message = "" +
	    		"<div style='border:1px solid #e2e2e2; padding:20px'>" +
	    		"<h1>" +
	    		"OTP is: " +
	    		"<b>"+otp+
	    		"<n>"+
	    		"</h1>"+
	    		"</div>" ;
	    
	    boolean flag = this.emailService.sendEmail(subject, message, email);
	    
		if(flag){
			session.setAttribute("myotp",otp );
			session.setAttribute("email", email);
			return "verify_otp";
		}else {
			//here it checks whether the mail in real exists or not
			session.setAttribute("messages", "Provide a valid Email id!!");
			return "forgot_email_form";	
		}
		
	}
	
     //verifying otp
	@PostMapping("/verify_otp")
	public String verifyOTP(@RequestParam("otp") int  otp, HttpSession session) {
		int myOtp= (int)session.getAttribute("myotp");
		String email=(String)session.getAttribute("email");
		if(myOtp==otp) {
			//password change form
			User user=this.userRepository.getUserByUserName(email);
			
			
			if(user==null) {
				//send error message
				session.setAttribute("messages", "No user with this email exists .");
				return "forgot_email_form";	
			}else {
				//send change password form
			}
		}else {
			session.setAttribute("messages", "You have entered the wrong otp");
			return "verify_otp";
		}
		return "password_change_form";
	}
	
	//change_password
	@PostMapping("/change_password")
	public String ChangePassword(@RequestParam("newpassword") String newpassword, HttpSession session) {
		String email=(String)session.getAttribute("email");
		User user=this.userRepository.getUserByUserName(email);
		user.setPassword(this.bcrypt.encode(newpassword));
		this.userRepository.save(user);
		return "redirect:/signin?change=Password changed successfully.";
	}
	
}
