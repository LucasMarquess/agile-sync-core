package com.agilesync.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "trello_settings")
@Getter
@Setter
public class TrelloSettings extends IntegrationSettings {

	@Column(name = "board_id")
	private String boardId;
}
