package com.agilesync.repository;

import com.agilesync.domain.entity.UserIntegrationsSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserIntegrationsSettingsRepository extends JpaRepository<UserIntegrationsSettings, Long> {
	Optional<UserIntegrationsSettings> findByUserId(Long userId);
}
