package com.agilesync.domain.dto;

import com.agilesync.domain.enumeration.UserRole;
import lombok.Getter;

@Getter
public class UserDTO {
	private String login;
	private String password;
	private String email;
	private UserRole role;
}
