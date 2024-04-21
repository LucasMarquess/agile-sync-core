package com.agilesync.controllers;

import com.agilesync.domain.dto.AuthenticationDTO;
import com.agilesync.domain.dto.LoginResponseDTO;
import com.agilesync.domain.dto.ResponseDTO;
import com.agilesync.domain.dto.UserDTO;
import com.agilesync.domain.model.User;
import com.agilesync.service.AuthorizationService;
import com.agilesync.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("auth")
@CrossOrigin("*")
public class AuthenticationController {

	@Autowired
	private TokenService tokenService;
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private AuthorizationService authorizationService;

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody @Valid AuthenticationDTO data) {
		if (Objects.isNull(authorizationService.loadUserByUsername(data.getLogin()))) {
			return ResponseEntity
					.status(HttpStatus.NOT_FOUND)
					.body(new ResponseDTO("Usuário não encontrado."));
		}

		try {
			UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(data.getLogin(), data.getPassword());
			Authentication auth = this.authenticationManager.authenticate(usernamePassword);

			String token = this.tokenService.generateToken((User) auth.getPrincipal());
			return ResponseEntity.ok(new LoginResponseDTO(token));
		} catch (BadCredentialsException ex) {
			return ResponseEntity
					.status(HttpStatus.UNAUTHORIZED)
					.body(new ResponseDTO("Senha incorreta."));
		}
	}

	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@RequestBody @Valid UserDTO dto) {
		if (Objects.nonNull(authorizationService.loadUserByEmail(dto.getEmail()))) {
			return ResponseEntity
					.status(HttpStatus.CONFLICT)
					.body(new ResponseDTO("Já existe um usuário utilizando o email: " + dto.getEmail() + "."));
		} else if (Objects.nonNull(authorizationService.loadUserByUsername(dto.getLogin()))) {
			return ResponseEntity
					.status(HttpStatus.CONFLICT)
					.body(new ResponseDTO("Já existe um usuário com este nome: " + dto.getLogin() + "."));
		}

		authorizationService.registerUser(dto);
		return ResponseEntity.ok().build();
	}
}
