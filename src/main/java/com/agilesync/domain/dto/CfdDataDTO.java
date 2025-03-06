package com.agilesync.domain.dto;

import com.agilesync.domain.enumeration.ScrumTrelloEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CfdDataDTO {
	private ScrumTrelloEnum stage;
	private Integer quantity;
	private String sprint;
}
