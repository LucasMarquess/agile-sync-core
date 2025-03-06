package com.agilesync.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MetricsDTO {
	private SprintCfdDataDTO sprintCfdData;
}
