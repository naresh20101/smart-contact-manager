package com.smart.controller;

import java.io.File;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
import com.smart.helper.Message;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String username = principal.getName();
		System.out.println("Email : " + username);
		User user = this.userRepository.getUserByUserName(username);
		System.out.println("User : " + user);
		model.addAttribute("user", user);
	}

	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal)
	{
		
		model.addAttribute("title", "User-Dashboard");
		return "normal/user_dashboard";
	}
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	@PostMapping("/process-contact")
	public String processContact(@Valid @ModelAttribute("contact") Contact contact,BindingResult result,@RequestParam("contact.imageURL") MultipartFile file,Principal principal,Model model, HttpSession session)
	{
		try {
			if(result.hasErrors())
			{
				System.out.println("Eroor"+ result.toString());
				 model.addAttribute("contact",contact);
				return"normal/add_contact_form";
			}
			String name=principal.getName();
			User user=userRepository.getUserByUserName(name);
			
			System.out.println("Contact"+ contact);
			if(file.isEmpty())
			{
				System.out.println("Its is empty");
				contact.setImage("download.png");
			}
			
		    
		else {
			contact.setImage(file.getOriginalFilename());
			File saveFile = new ClassPathResource("static/img").getFile();
			Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Image is Upload");
			
		}
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Your contact is added","success"));
	}
		 catch (Exception e) {
			 e.printStackTrace();
			// TODO: handle exception
			 //Error message
			 session.setAttribute("message", new Message("Something went wrong","danger"));
		}
		
		return "normal/add_contact_form";
	}
	//show All COntacts
	@GetMapping("/show-contacts/{currentPage}")
	public String showContacts(@PathVariable("currentPage") Integer currentPage,  Model m, Principal principal)
	{
		m.addAttribute("title","Show User Contacts");
		String userName=principal.getName();
		Pageable pageable=PageRequest.of(currentPage, 5);
		User user=this.userRepository.getUserByUserName(userName);
		Page<Contact> contacts=this.contactRepository.findContactsByUser(user.getId(),pageable);
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage", currentPage);
		m.addAttribute("totalPages", contacts.getTotalPages());
		
		
		return "normal/show_contacts";
	}
	//showing details of particular contact
	@RequestMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId,Model model, Principal principal)
	{
		String username=principal.getName();
		User user=this.userRepository.getUserByUserName(username);
		System.out.println("CID"+ cId);
		Optional<Contact> contOptional=this.contactRepository.findById(cId);
		Contact contact=contOptional.get();
		if(user.getId()==contact.getUser().getId())
		    model.addAttribute("contact", contact);
		return "normal/show_contact_details";
	}
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model model, HttpSession session,
			Principal principal) {
		Contact contact = this.contactRepository.findById(cId).get();
		System.out.println("Contact : " + contact.getcId());
		User user = this.userRepository.getUserByUserName(principal.getName());
		//contact.setUser(null);
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		this.contactRepository.delete(contact);
		System.out.println("Deleted");
		session.setAttribute("message", new Message("Contact deleted Sucessfully...", "success"));
		return "redirect:/user/show-contacts/0";
	}
	// Open Update Form Handler
    @PostMapping("/update-contact/{cId}")
    public String updateForm(@PathVariable("cId") Integer cId, Model model) {
			model.addAttribute("title", "Update-Contact");

			Contact contact = this.contactRepository.findById(cId).get();
			model.addAttribute("contact", contact);
			return "normal/update_form";
		}
    //process update form
    @RequestMapping(value="/process-update", method=RequestMethod.POST)
    public String updateHandler(@ModelAttribute Contact contact, @RequestParam("contact.imageURL") MultipartFile file,Principal principal,Model model, HttpSession session)
    {
    	try {
    		
    		User user=this.userRepository.getUserByUserName(principal.getName());
    		Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
    		if (!file.isEmpty()) {
				// Delete Old Photo Form Computer
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file2 = new File(deleteFile, oldContactDetail.getImage());
				file2.delete();
				// Update New Photo
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			} else {
				contact.setImage(oldContactDetail.getImage());
			}
    		contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Your Contact is Updated....", "success"));
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	System.out.println("Name"+ contact.getName());
    	System.out.println("Id"+ contact.getcId());
    	return"redirect:/user/" + contact.getcId() + "/contact";
    }
    //profile handler
    @GetMapping("/profile")
    public String profileHandler(Model m)
    { 
    	m.addAttribute("title","Profile-Page");
    	return "normal/profile";
    }
    //setting handler
    @GetMapping("/settings")
    public String settings()
    {
    	return "normal/setting";
    }
    //changing password handler
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword,  Principal principal, HttpSession session)
    {
    	String userName=principal.getName();
    	User currentUser=this.userRepository.getUserByUserName(userName);
    	System.out.println("cuurent user"+ currentUser.getPassword());
    	
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your Password Successfully Change","success"));
		}
		else {
			session.setAttribute("message", new Message("Please Enter correct Old Password !!","danger"));
			return "redirect:/user/settings";
		}
		return "redirect:/user/index";
    	
    }
}
