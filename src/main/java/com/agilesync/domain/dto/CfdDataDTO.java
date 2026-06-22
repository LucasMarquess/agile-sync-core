package com.agilesync.domain.dto;

import com.agilesync.domain.enumeration.ScrumStagesEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CfdDataDTO {
	private ScrumStagesEnum stage;
	private Integer quantityTotal;
	private Integer quantityCards;
	private String sprint;

	public String getstageReport() {
		return stage.toString();
	}
}
