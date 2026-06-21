package com.agilesync.domain.dto;

import lombok.Data;

@Data
public class TrelloLabelDTO implements LabelDTO {
	private String id;
	private String name;
}