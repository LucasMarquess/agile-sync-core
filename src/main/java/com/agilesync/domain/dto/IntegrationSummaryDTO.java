package com.agilesync.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IntegrationSummaryDTO {
	private Long id;
	private String name;
	private String type;
	private boolean fullyConfigured;
	private boolean active;
}
