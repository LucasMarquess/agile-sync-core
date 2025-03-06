package com.agilesync.repository;

import com.agilesync.domain.entity.TrelloSettings;
import com.agilesync.service.IntegrationsService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TrelloSettingsRepository extends JpaRepository<TrelloSettings, Long> {

	@Query(" SELECT uis.trelloSettings FROM UserIntegrationsSettings uis WHERE uis.id = :userId")
	TrelloSettings findByUserId(Long userId);

	@Query(value = """
	SELECT CASE
		WHEN EXISTS (
			SELECT 1
			FROM user_integrations_settings uis
			INNER JOIN trello_settings ts ON uis.trello_settings_id = ts.id
			WHERE uis.user_id = :userId
			AND ts.token IS NOT NULL AND ts.token <> ''
			AND ts.board_id IS NOT NULL AND ts.board_id <> ''
			AND EXISTS (
				SELECT 1
				FROM trello_mapping tm
				WHERE tm.trello_settings_id = ts.id
				AND tm.referent IS NOT NULL AND tm.referent <> ''
				AND tm.list_name IS NOT NULL AND tm.list_name <> ''
				AND tm.list_id IS NOT NULL AND tm.list_id <> ''
			)
		)
		THEN true ELSE false
	END
    """, nativeQuery = true)
	boolean verifyUserHasIntegration(Long userId);


}
