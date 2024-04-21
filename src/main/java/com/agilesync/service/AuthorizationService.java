package com.agilesync.service;

import com.agilesync.domain.dto.UserDTO;
import com.agilesync.domain.enumeration.UserRole;
import com.agilesync.domain.model.User;
import com.agilesync.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
		String encryptedPassword = new BCryptPasswordEncoder().encode(dto.getPassword());
		User newUser = new User(dto.getLogin(), encryptedPassword, dto.getEmail(), UserRole.USER);

		this.userRepository.save(newUser);
	}
}
