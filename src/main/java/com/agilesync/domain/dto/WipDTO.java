package com.agilesync.domain.dto;

import com.agilesync.domain.enumeration.ScrumTrelloEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WipDTO {
	private ScrumTrelloEnum stage;
	private Integer quantity;
}
