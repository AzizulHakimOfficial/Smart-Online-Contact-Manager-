package com.project.contact.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.project.contact.dao.ContactRepository;
import com.project.contact.dao.UserRepository;
import com.project.contact.entities.Contact;
import com.project.contact.entities.User;
import com.project.contact.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;
	
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model,Principal principal,HttpSession sess) {
		
		if(sess!=null) {
			sess.removeAttribute("message");
		}
		String userName=principal.getName();
		//get the user using username
		User user = userRepository.getUserByUserName(userName);
		model.addAttribute("user",user);
	}
	
	
	//dashboard home
	
	@GetMapping("/index")
	public String dashboard(Model model,Principal principal) {
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			
			//processing and uploading file
			if(file.isEmpty()) {
				contact.setImage("contact.png");
			}else {
				//file Upload to folder
				contact.setImage(file.getOriginalFilename());
				File saveFile=new ClassPathResource("static/img").getFile();
				
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
			}
			
			contact.setUser(user);
			user.getContacts().add(contact);

			this.userRepository.save(user);
			
			session.setAttribute("message", new Message("Your Contact is added. Fill Again to Add more!! ","success"));
			
		}catch(Exception e) {
			session.setAttribute("message", new Message("Something went wrong!Try again! ","danger"));
		}
		return "normal/add_contact_form";
	}
	
	//show contatcs handler
	//per page 5rows
	//current Page=0[page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model model,Principal principal,HttpSession session) {
		model.addAttribute("title","Show Contacts");
		//send contact list
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		Pageable pageable = PageRequest.of(page, 3);
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		model.addAttribute("contacts",contacts);
		model.addAttribute("currentPage",page);
		session.setAttribute("currentPage", page);
		model.addAttribute("totalPages",contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	//shwoing particular contact details
	@GetMapping("/{id}/contact")
	public String showContactDetail(@PathVariable("id")Integer id,Model model,Principal principal) {
		model.addAttribute("title","Show Contacts Details");
		Optional<Contact> contactOptional = this.contactRepository.findById(id);
		Contact contact= contactOptional.get();
		//finding User
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId()) {
			model.addAttribute("contact",contact);
			model.addAttribute("title",contact.getName());
		}
		return "normal/contact_detail";
	}
	
		//delete contact Handler
	@GetMapping("/delete/{id}")
	@Transactional
	public String deleteContatct(@PathVariable("id") Integer id,Model model,Principal principal,HttpSession session) {
		
		Optional<Contact> contactOptional =this.contactRepository.findById(id);
		Contact contact= contactOptional.get();
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		session.setAttribute("message", new Message("Contact Deleted Successfully!!","success"));
		
		/*if(user.getId()==contact.getUser().getId()) {
			
			contact.setUser(null);
			model.addAttribute("contact",contact);
			model.addAttribute("title",contact.getName());
			//remove photo assignment
			this.contactRepository.delete(contact);
			session.setAttribute("message", new Message("Contact Deleted Successfully!!","success"));
		}*/
		
		return "redirect:/user/show-contacts/0";
	}
	
	//update Contact Handler
	//delete and update handler same kora jabe..ekta get+if diye kora hoice r post ta if chara user korar jnno kor hoice.
	@PostMapping("/update-contact/{id}")
	public String updateContact(@PathVariable("id")Integer id,Model model) {
		model.addAttribute("title","Update Contact");
		Contact contact = this.contactRepository.findById(id).get();
		model.addAttribute("contact",contact);
		return "normal/update_form";
	}
	
	//update contact handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {
		try {
			
			Contact oldContactDetails=this.contactRepository.findById(contact.getId()).get();
			
			//processing and updating file
			if(file.isEmpty()) {
				contact.setImage(oldContactDetails.getImage());
			}else {
				
				//delete old folder
				File deleteFile=new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile,oldContactDetails.getImage());
				if(!"contact.png".equals(oldContactDetails.getImage())) {
					file1.delete();
				}
				//file Upload to folder
				
				File saveFile=new ClassPathResource("static/img").getFile();
				
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}
			
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			contact.setUser(user);

			this.contactRepository.save(contact);
			//int page=(int)session.getAttribute("currentPage");
			
			session.setAttribute("message", new Message("Your Contact is Updated.","success"));
			
		}catch(Exception e) {
			session.setAttribute("message", new Message("Something went wrong!Try again! ","danger"));
		}
		return "redirect:/user/show-contacts/"+session.getAttribute("currentPage");
	}
	
	//User Profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title","Profile");
		return "normal/profile";
	}
	
	//open setting handler
	@GetMapping("/settings")
	public String openSetting(Model model) {
		model.addAttribute("title","Settings");
		return "normal/settings";
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session) {
		
		String userName= principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			//change password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your password is successsfully changed. ","success"));
		}else {
			session.setAttribute("message", new Message("Your old is wrong.Please enter correct old password & try A=again! ","danger"));
			return "redirect:/user/settings";
		}
		return "redirect:/user/index";
	}
	
	
}

	