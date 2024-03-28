package com.agilesync.controllers;

import com.agilesync.domain.dto.AuthenticationDTO;
import com.agilesync.domain.dto.LoginResponseDTO;
import com.agilesync.domain.dto.UserDTO;
import com.agilesync.domain.model.User;
import com.agilesync.repository.UserRepository;
import com.agilesync.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
public class AuthenticationController {

	@Autowired
	private UserRepository repository;
	@Autowired
	private TokenService tokenService;
	@Autowired
	private AuthenticationManager authenticationManager;

	@PostMapping("/login")
	public ResponseEntity login(@RequestBody @Valid AuthenticationDTO data){
		var usernamePassword = new UsernamePasswordAuthenticationToken(data.getLogin(), data.getPassword());
		var auth = this.authenticationManager.authenticate(usernamePassword);

		var token = tokenService.generateToken((User) auth.getPrincipal());

		return ResponseEntity.ok(new LoginResponseDTO(token));
	}

	@PostMapping("/register")
	public ResponseEntity register(@RequestBody @Valid UserDTO dto){
		if(this.repository.findByLogin(dto.getLogin()) != null) return ResponseEntity.badRequest().build();

		String encryptedPassword = new BCryptPasswordEncoder().encode(dto.getPassword());
		User newUser = new User(dto.getLogin(), encryptedPassword, dto.getEmail(), dto.getRole());

		this.repository.save(newUser);

		return ResponseEntity.ok().build();
	}
}
