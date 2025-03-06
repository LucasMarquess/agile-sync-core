package com.agilesync.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SprintCfdDataDTO {
	Integer sprintNumber;
	String sprintName;
	List<CfdDataDTO> cfdDatas;
}
