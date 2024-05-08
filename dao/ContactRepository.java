package com.smart.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java .util.*;
import com.smart.entities.Contact;
import com.smart.entities.User;

public interface ContactRepository  extends JpaRepository<Contact, Integer>{
 ///this contact Repository can be used for both pagination and to fetch contact details
	//when you want to show your information/contacts in more than one page becoz if you load it one page the website loading can be slow
	//using page we can  gain information about a position 
	//pageable is an interface to store pagination information, it conatins 2 datas , one is the no. of items per page and the other is the current page
	//currentpage=page
	@Query("from Contact as c where c.user.id=:userId ")
	public Page<Contact> findContactByUser(@Param("userId")int userId, Pageable pePageable);
	//here we are using  user to ensure all the contacts in the db dont appear  on search results only the user specific ones appear 
	public List<Contact> findByNameContainingAndUser(String name , User user);
}
