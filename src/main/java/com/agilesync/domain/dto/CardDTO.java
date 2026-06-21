package com.agilesync.domain.dto;

import java.util.List;

public interface CardDTO {
	String getId();

	String getName();

	List<? extends LabelDTO> getLabels();
}
