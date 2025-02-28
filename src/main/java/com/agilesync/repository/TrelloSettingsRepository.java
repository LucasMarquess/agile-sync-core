package com.agilesync.repository;

import com.agilesync.domain.entity.TrelloSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TrelloSettingsRepository extends JpaRepository<TrelloSettings, Long> {

	@Query(" SELECT uis.trelloSettings FROM UserIntegrationsSettings uis WHERE uis.id = :userId")
	TrelloSettings findByUserId(Long userId);
}
