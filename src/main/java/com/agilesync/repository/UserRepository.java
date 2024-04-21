package com.agilesync.repository;

import com.agilesync.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserRepository extends JpaRepository<User, Long> {
	UserDetails findByLogin(String email);
	UserDetails findByEmail(String email);
}
