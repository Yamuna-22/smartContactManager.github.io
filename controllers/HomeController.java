package com.smart.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Messages;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	//Creating an object to use the pre-defined methods from JpaRepository
	@Autowired
	private UserRepository  userRepository ;
	
	/* Lead to home.html page */
   @RequestMapping("/")
	public String home(Model m) {
	   m.addAttribute("title" ,"Home-Smart-Contact-Manager");
	   return "home";
	}
   
   
	/* to run the about.html */
   @RequestMapping("/about")
    public String about(Model m) {
	   m.addAttribute("title" ,"about-Smart-Contact-Manager");
       return "about";
		
	}
   
	/* Leads to the signUp page */
   @RequestMapping("/register")
   public String register(Model m) {
	   m.addAttribute("title" ,"Register-Smart-Contact-Manager");
	   m.addAttribute("user", new User());
		return "signup";
		
	}

   // to handle and process the user derails recieved from the sign up form

	// a seperate  @requestParam for agreement , because this field is absent in User Entiry
	 
   @RequestMapping(value="/do_register" , method=RequestMethod.POST)
	public String register(@Valid @ModelAttribute("user") User user ,BindingResult result1, @RequestParam(value="agreement" , defaultValue="false") boolean agreement,  Model m, HttpSession session) {
	 try { 
		 
		 //result1 for server side validation errors
		 if(result1.hasErrors()) {
			  System.out.println("ERROR"+ result1.toString());
			  //to send the data back to the  form ,the data you have entered remains preserved even signup is reloaded again
			  m.addAttribute("user", user);
			  return "signup";
		  }
		   
		 //if terms and conditions checkbox is not checked
		 if(!agreement) {
		 System.out.println("You have not agreed to the terms and  conditions.") ;
		 throw new Exception("You have not agreed to the terms and conditions.");
	  }
	  

	  user.setRole("ROLE_USER");
	  user.setEnabled(true);
	  user.setImageUrl("dafault.png");
	  user.setPassword(passwordEncoder.encode(user.getPassword()));
	  
	  System.out.println("Agreement" +agreement);
	  System.out.println("User" + user);
	 
		/* to save the user data to DataBase */
	  User result= this.userRepository.save(user);
	  m.addAttribute("user", new User());
	  session.setAttribute("message", new Messages("Successfully registered","alert-success"));
	  return "signup";
	  }catch(Exception e) {
		  e.printStackTrace();
		  m.addAttribute("user", user);
		  session.setAttribute("message", new Messages("Something Went wrong!!"+ e.getMessage(),"alert-danger"));
		  return "signup";
	  }
	
	}
   
    @RequestMapping("/signin")
  	public String CustomLogIn(Model m) {
	   m.addAttribute("title" ,"LogIn");
  		
  		return "login";
  		
  	}

}
