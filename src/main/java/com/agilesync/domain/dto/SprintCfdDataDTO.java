package com.agilesync.domain.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SprintCfdDataDTO {
	Integer sprintNumber;
	List<CfdDataDTO> cfdDatas;

	private List<WipDTO> wipsByStage;
	private Integer throughput;
	private BigDecimal leadTime;
	private BigDecimal cycleTime;
}
