package com.project.contact.dao;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.project.contact.entities.Contact;
import com.project.contact.entities.User;

public interface ContactRepository extends CrudRepository<Contact, Integer>{

	//currentPage-page
	//Contact Per page - 5
	@Query("from Contact as c where c.user.id =:userId")
	public Page<Contact> findContactsByUser(@Param("userId")int userId, Pageable pageable);
	
	//search method
	public List<Contact> findByNameContainingAndUser(String name, User user);
	
}
