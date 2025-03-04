package com.agilesync.repository;

import com.agilesync.domain.entity.TrelloMapping;
import com.agilesync.domain.enumeration.ScrumTrelloEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrelloMappingRepository extends JpaRepository<TrelloMapping, Long> {
	List<TrelloMapping> getTrelloMappingsByTrelloSettingsId(Long trelloSettings_id );

	TrelloMapping findByTrelloSettingsIdAndListName(Long trelloSettingsId, String listName);

	TrelloMapping findByTrelloSettingsIdAndReferent(Long trelloSettingsId, ScrumTrelloEnum referent);
}