package com.agilesync.domain.dto;

import com.agilesync.domain.enumeration.ScrumStagesEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WipDTO {
	private ScrumStagesEnum stage;
	private Integer quantity;
	private Integer sprintNumber;

	public WipDTO(ScrumStagesEnum stage, Integer quantity) {
		this.stage = stage;
		this.quantity = quantity;
	}

	public String getstageReport() {
		return stage.getDescription();
	}
}
