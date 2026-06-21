package com.agilesync.repository;

import com.agilesync.domain.entity.TrelloSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TrelloSettingsRepository extends JpaRepository<TrelloSettings, Long> {

	List<TrelloSettings> findAllByUserIdAndActiveTrue(Long userId);

	List<TrelloSettings> findAllByUserId(Long userId);

	Optional<TrelloSettings> findByIdAndUserId(Long id, Long userId);

	boolean existsByUserIdAndBoardIdAndToken(Long userId, String boardId, String token);

	@Query(value = """
			SELECT CASE
				WHEN EXISTS (
					SELECT 1
					FROM trello_settings ts
					WHERE ts.user_id = :userId
					AND ts.active = true
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

	@Query(value = """
			SELECT CASE
				WHEN EXISTS (
					SELECT 1
					FROM trello_settings ts
					WHERE ts.id = :trelloSettingsId
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
	boolean verifyIntegrationIsConfigured(Long trelloSettingsId);

}
