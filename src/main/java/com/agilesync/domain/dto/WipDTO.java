package com.agilesync.domain.dto;

import com.agilesync.domain.enumeration.ScrumTrelloEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WipDTO {
	private ScrumTrelloEnum stage;
	private Integer quantity;
	private Integer sprintNumber;

	public WipDTO(ScrumTrelloEnum stage, Integer quantity) {
		this.stage = stage;
		this.quantity = quantity;
	}

	public String getstageReport() {
		return stage.getDescription();
	}
}
