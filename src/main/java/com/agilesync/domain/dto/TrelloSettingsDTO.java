package com.agilesync.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrelloSettingsDTO {
	private Long id;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String token;
	private String key;
	private String cardMappingName;
	private String boardId;
}
