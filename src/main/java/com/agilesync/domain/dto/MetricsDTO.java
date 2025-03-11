package com.agilesync.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class MetricsDTO {
	private BigDecimal velocity;
	private List<SprintCfdDataDTO> sprintCfdData;
}
