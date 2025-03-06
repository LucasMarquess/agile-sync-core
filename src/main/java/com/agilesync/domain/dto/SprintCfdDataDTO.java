package com.agilesync.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SprintCfdDataDTO {
	Integer sprintNumber;
	List<CfdDataDTO> cfdDatas;

	private List<WipDTO> wipsByStage;
	private Integer throughput;
	private Double leadTime;
	private Double cycleTime;
}
