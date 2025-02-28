package com.agilesync.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "trello_settings")
@Getter
@Setter
public class TrelloSettings extends IntegrationSettings {

	@Column(name = "card_mapping_name")
	private String cardMappingName;

	@Column(name = "board_id")
	private String boardId;
}
