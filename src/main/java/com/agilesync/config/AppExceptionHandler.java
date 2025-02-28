package com.agilesync.config;

import com.agilesync.domain.dto.ErrorResponseDTO;
import com.agilesync.exceptions.BadRequestException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class AppExceptionHandler {

	@ExceptionHandler(value = { BadRequestException.class})
	public ResponseEntity<Object> handleUserServiceException(BadRequestException ex, WebRequest request) {
		return new ResponseEntity<Object>(new ErrorResponseDTO(LocalDateTime.now(), ex.getMessage()), new HttpHeaders(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(value = {Exception.class})
	public ResponseEntity<Object> handleException(Exception ex, WebRequest request) {
		return new ResponseEntity<>(new ErrorResponseDTO(LocalDateTime.now(), ex.getMessage()), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

}