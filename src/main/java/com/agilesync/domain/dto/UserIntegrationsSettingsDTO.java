package com.agilesync.domain.dto;

import lombok.Data;

@Data
public class UserIntegrationsSettingsDTO {
	private Long userId;
	private TrelloSettingsDTO trelloSettings;
}