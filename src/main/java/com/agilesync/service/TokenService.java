package com.agilesync.service;

import com.agilesync.domain.entity.User;
import com.agilesync.exceptions.TokenException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

	@Value("${api.security.token.secret}")
	private String secret;

	public String generateToken(User user){
		try{
			Algorithm algorithm = Algorithm.HMAC256(secret);
			return JWT.create()
					.withIssuer("agile-sync-core")
					.withExpiresAt(genExpirationDate())
					.withSubject(user.getLogin())
					.withClaim("role", user.getRole().toString())
					.sign(algorithm);
		} catch (JWTCreationException exception) {
			throw new TokenException("Error while generating token", exception);
		}
	}

	public String validateToken(String token){
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
			return JWT.require(algorithm)
					.withIssuer("auth-api")
					.build()
					.verify(token)
					.getSubject();
		} catch (JWTVerificationException exception){
			return "";
		}
	}

	private Instant genExpirationDate(){
		return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
	}
}
