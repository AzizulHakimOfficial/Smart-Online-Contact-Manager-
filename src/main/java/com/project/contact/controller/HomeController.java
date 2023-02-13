package com.project.contact.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.contact.dao.UserRepository;
import com.project.contact.entities.User;
import com.project.contact.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController{
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;

	@ModelAttribute
	public void addCommonData(HttpSession sess) {
		
		if(sess!=null) {
			sess.removeAttribute("message");
		}
	}
	
	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title","Home - Smart Contact Manager");
		return "home";
	}
	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("title","About - Smart Contact Manager");
		return "about";
	}
	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title","Register - Smart Contact Manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	
	//this is handler for registering
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1, @RequestParam(value="enabled", defaultValue = "false") Boolean enabled,Model model, HttpSession session) {
		try {
			if(!enabled) {
				//throw new Exception("You have Not agreed terms and Conditions.");
				model.addAttribute("user",user);
				return "signup";
			}
			if(result1.hasErrors()) {
				model.addAttribute("user",user);
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			this.userRepository.save(user);
			
			model.addAttribute("user",new User());
			session.setAttribute("message", new Message("Successfully Registered!","alert-success"));
			return "signup";
			
		}catch(Exception e) {
		model.addAttribute("user",user);
		session.setAttribute("message", new Message("Something went wrong! "+e.getMessage(),"alert-danger"));
		return "signup";
		}
		
	}
	@GetMapping("signin")
	public String customLogin(Model model) {
		model.addAttribute("titile","Login - Smart Contact Manager");
		return "login";
	}
}
