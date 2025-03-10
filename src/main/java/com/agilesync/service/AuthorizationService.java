package com.agilesync.service;

import com.agilesync.domain.dto.UserDTO;
import com.agilesync.domain.enumeration.UserRole;
import com.agilesync.domain.entity.User;
import com.agilesync.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByLogin(username);
	}

	public UserDetails loadUserByEmail(String email){
		return userRepository.findByEmail(email);
	}

	public void registerUser(UserDTO dto){
		var encryptedPassword = new BCryptPasswordEncoder().encode(dto.getPassword());
		var newUser = new User(dto.getLogin(), encryptedPassword, dto.getEmail(), UserRole.USER);

		this.userRepository.save(newUser);
	}

	public User getCurrentUser() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		return (User) authentication.getPrincipal();
	}
}
