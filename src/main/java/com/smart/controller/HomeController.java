package com.smart.controller;


import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;


@Controller
public class HomeController {
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository userRepository;
	@RequestMapping("/")
	public String home(Model model)
	{
		model.addAttribute("title","Home-Smart Contact Manager");
		return "home";
	}
	@RequestMapping("/about")
	public String about(Model model)
	{
		model.addAttribute("title","About-Smart Contact Manager");
		return "about";
	}
	@RequestMapping("/signup")
	public String signUp(Model model)
	{
		model.addAttribute("title","Register-Smart Contact Manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	@RequestMapping(value="/doRegister", method=RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult results,@RequestParam(value="aggrement",defaultValue = "false") boolean aggrement ,Model model, HttpSession session)
	{
		try {
			if(!aggrement)
			{
				throw new Exception(" You have not Accept terms & conditions");
			}
			if(results.hasErrors())
			{
				System.out.println("Eroor"+ results.toString());
				 model.addAttribute("user",user);
				return "signup";
			}
			System.out.println("Arjg"+ aggrement);
			System.out.println("User"+ user);
			   user.setEnabled(true);
			   user.setRole("ROLE_USER");
			   user.setPassword(passwordEncoder.encode(user.getPassword()));
			  this.userRepository.save(user);
			  model.addAttribute("user",new User());
			  session.setAttribute("message", new Message( "Succesfully Registered!!", "alert-success"));
			return "signup";
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			 
			session.setAttribute("message", new Message("Something went wrong!!"+e.getMessage(), "alert-danger"));
			return "signup";
		}
		
	}
	@RequestMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title","Login-Smart Contact Manager");
		return "login";
	}
}

