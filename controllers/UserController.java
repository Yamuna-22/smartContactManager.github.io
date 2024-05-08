 package com.smart.controllers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.*;



import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Messages;

import jakarta.servlet.http.HttpSession;



@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository ;
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model m , Principal principal) {
		String userName=principal.getName();
		System.out.println("USERNAME:"+userName);
		
		User user=this.userRepository.getUserByUserName(userName);
		System.out.println("USER"+user);
		
		m.addAttribute("user", user);		//get the user using username(Email)
		
	}
	
	//dashboard home
	
	@RequestMapping("/index")
	public String dashBoard(Model m , Principal principal) {
		m.addAttribute("title", "User DashBoard");
		
		return "normal/user_dashborad";
		
	}
	
	//handler to open add contact form
	@GetMapping("/add-contact")
	public String openAddContactForm(Model m) {
		m.addAttribute("title", "Add Contact");
		m.addAttribute("conatct", new Contact());
		
		
		return "normal/add_contact_form";
	}
	
	//to process the contact
	//here in process contact method we have 3 parameters ,model attribute to fetch the details of the contact , requestParam for image and principal to fetch the user name
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file ,Model m,Principal principal, HttpSession session) {
		
		/*
		 * try { int phoneNumber = Integer.parseInt(contact.getPhone());
		 * contact.setPhone(phoneNumber); } catch (NumberFormatException e) {
		 * e.printStackTrace(); }
		 */
		try {
		String name =principal.getName();
		
		User user=this.userRepository.getUserByUserName(name);
		
		//processing and upload image
		
		if(file.isEmpty()) {
			System.out.println("File is Empty");
			contact.setImageUrl("contact.jpg");
			
		}
		else {
			//save the file to folder and update the name to contact
			contact.setImageUrl((file.getOriginalFilename()));
			
	    File saveFile=	new ClassPathResource("static/img").getFile();
		//Files.copy takes all the bytes from the source and give it to the destination path
	    //Here we are uploading the exact name of the file given by  the user , and it may happen that 2 users are providing the images with same file name , for such cases you can also append the username or id   at the end , to make it unique.
	    //import paths from import java.nio.file.Path;
	    //when you right click on your project and go for system explorer than , in our project folder and img in static , we can't find the  image file uploaded  as static contains all the files uploaded manually
	    //target is very important as it is built whenever our project runs , so in target the image gets uploaded
	    //when you clean the project , the photo will be removed
	    //when you deploy the project ,there wont be such an issue as one folder is created.
	    
		Path path =Paths.get(saveFile.getAbsolutePath()+ File.separator+ file.getOriginalFilename());
		Files.copy(file.getInputStream(),path , StandardCopyOption.REPLACE_EXISTING );
		
		System.out.println("Image Uploaded");
		}
		user.getContacts().add(contact);
		contact.setUser(user);
		
		
		this.userRepository.save(user);
		System.out.println("DATA" +contact);
		System.out.println("Successfully added the contact");
		//message success
		m.addAttribute("session", session);
		session.setAttribute("messages", new Messages("Your contact has been sucessfully saved!! Add more contacts", "success"));
	
		}catch(Exception e) {
			System.out.println("ERROR" +e.getMessage());
			e.printStackTrace();
			//message error
			
			session.setAttribute("messages", new Messages("Something went wrong", "danger"));
		}
	
		return "normal/add_contact_form";
	}
	
	
	
	//show contacts handler
	//per page only 5 contacts =5[n]
	//current page=0[page]
	@GetMapping("/show_contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page   ,Model m, Principal principal) {
		m.addAttribute("title", "Show_User_Contacts");
		//sending contacts list -method 1 
		/*
		 * String userName=principal.getName(); User user
		 * =this.userRepository.getUserByUserName(userName); List<Contact> contacts =
		 * user.getContacts();
		 */
 		//method 2 is to send data to show contacts is through creatig a seperate Contact Repository
	
		String userNmae=principal.getName();
		User user = this.userRepository.getUserByUserName(userNmae);

		//currentPage-page
		//Contact per page-5
		PageRequest pageable=PageRequest.of(page, 5);
		Page<Contact> contacts=this.contactRepository.findContactByUser(user.getId(), pageable);
		
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		
	
		return "normal/show_contacts";
	}
	
	
	
	//to display a particular contact on clicking on email
	@GetMapping("contact/{cId}")
	public String showContactDetails(@PathVariable ("cId") Integer cId, Model m, Principal principal) {
		System.out.println("CID"+cId);
		Optional<Contact> contactOptional=this.contactRepository.findById(cId);
		Contact contact=contactOptional.get();
		//fixing security bug
		String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		
		if(user.getId()== contact.getUser().getId()) {
		m.addAttribute("contact", contact);}
		return "normal/contact_Details";
	}
	
	
	
	//handler to delete contact
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId")Integer cId, Model m, HttpSession session, Principal principal ) {
	 
		Contact contact=this.contactRepository.findById(cId).get();
		//as we have used cascade , so we have to first cut the link between the user and contact
	   //contact.setUser(null);
	   //here though we used jpa , the contact just got unlinked with the user , but did not get deleted  due to the usage of cascade
	    //this.contactRepository.delete(contact);
	 
		//match the objects by overriding equals method and finds the contact to be deleted and deletes it
		User user =this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
	 
	  session.setAttribute("messages", new Messages("Contact Deleted Successfully..","success"));
		return "redirect:/user/show_contacts/0";
	}
	
	
	//open update form handler
	@PostMapping("/updateContact/{cId}")
	public String updateForm(@PathVariable("cId")Integer cId,Model m) {
		m.addAttribute("title", "Update Contacts");
		Contact contact=this.contactRepository.findById(cId).get();
		m.addAttribute("contact", contact);
		return "normal/update_form";
	}
	
    // processing the update form 
	//to send back the data to view use model
	//to send message use session
	@RequestMapping(value="/update_Contact" , method= RequestMethod.POST)
	public String updateContact(@ModelAttribute Contact contact, @RequestParam("profileImage")MultipartFile file,Model m , HttpSession session, Principal principal) {
		try {
			//old image details
			Contact oldContactDetails=this.contactRepository.findById(contact.getcId()).get();
			//image
			if(!file.isEmpty()) {
				//file works and rewrite
				//delete old photo
				File deleteFile=	new ClassPathResource("static/img").getFile();
				File file1= new File(deleteFile ,oldContactDetails.getImageUrl());
				file1.delete();
				
				//add new photo
				File saveFile=	new ClassPathResource("static/img").getFile();
                Path path =Paths.get(saveFile.getAbsolutePath()+ File.separator+ file.getOriginalFilename());
                Files.copy(file.getInputStream(),path , StandardCopyOption.REPLACE_EXISTING );
				contact.setImageUrl(file.getOriginalFilename());
				
			}else {
				contact.setImageUrl(oldContactDetails.getImageUrl());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("messages", new Messages("Contact is updated Successfuuly","success"));
		}catch(Exception e){
			
		}
		
		
		
		System.out.println("CONTACT NAME "+contact.getName());
		System.out.println("CONTACT ID"+contact.getcId());
		return "redirect:/user/contact/"+contact.getcId();
	}
	
	// your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model){
	model.addAttribute("title" , "Profile page");
	
	
		return "normal/profile";
		
	}
	
	//open settings handler
	@GetMapping("/settings")
	public String openSettings(){
		
	return "normal/settings";	
	
	}
	
	//change password handler
	@PostMapping("/change_password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword , @RequestParam("newPassword") String newPassword , Principal principal, HttpSession session) {
		
		System.out.println("OLD PASSWORD" + oldPassword );
		System.out.println("NEW PASSWORD" + newPassword );
		String userName=principal.getName();
		User currentUser =this.userRepository.getUserByUserName(userName);
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
		//change the password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("messages", new Messages("Your Password is successfully changed","success"));
		}else {
			session.setAttribute("messages", new Messages("Please enter correct old password!!","danger"));
			return "redirect:/user/settings";
		}
		return "redirect:/user/index";
	}
	
	
	
	//ACCOUNT VERIFICATION
	
	
	// FORGET PASSWORD LOGIC 
	// OTP- one time password
	// on the client side we develop a forgot password button and create a handler to display a html page  for email and generating otp when clicked on the forgot password button.
	//on clicking on get otp server should randomly generate an OTP and send the opt to the email if valid , if the email is not valid then generate an error.
	// save the otp in key value pair in the session and generate a message , otp successfully sent ,it will be valid only for a limited period of time.
	//now it should generate a field to enter otp and submit, now the user has to check for otp and enter it in the field and if the otp matches,
	//then it should redirect to change password without any verification as the user is already verified through otp.
	//if the otp does not matches send an error.
	
	
	//TO VERIFY EMAIL DURING SIGN UP
	
       
	
	
}
