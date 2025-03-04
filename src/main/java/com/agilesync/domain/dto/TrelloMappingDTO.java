package com.agilesync.domain.dto;

import com.agilesync.domain.entity.TrelloMapping;
import com.agilesync.domain.entity.TrelloSettings;
import com.agilesync.domain.enumeration.ScrumTrelloEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrelloMappingDTO {
	private ScrumTrelloEnum referent;
	private String listId;
	private String listName;
	private Long trelloSettingId;

	public static TrelloMapping mapToEntity(TrelloMappingDTO dto, TrelloSettings trelloSettings) {
		TrelloMapping entity = new TrelloMapping();
		entity.setReferent(dto.getReferent());
		entity.setListId(dto.getListId());
		entity.setListName(dto.getListName());
		entity.setTrelloSettings(trelloSettings);
		return entity;
	}

	public static TrelloMappingDTO mapToDTO(TrelloMapping entity) {
		return new TrelloMappingDTO(
				entity.getReferent(),
				entity.getListId(),
				entity.getListName(),
				entity.getTrelloSettings().getId()
		);
	}
}
