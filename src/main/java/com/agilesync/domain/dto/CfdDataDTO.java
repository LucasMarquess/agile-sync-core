package com.agilesync.domain.dto;

import com.agilesync.domain.enumeration.ScrumTrelloEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CfdDataDTO {
	private ScrumTrelloEnum stage;
	private Integer quantityTotal;
	private Integer quantityCards;
	private String sprint;

	public String getstageReport() {
		return stage.toString();
	}
}
