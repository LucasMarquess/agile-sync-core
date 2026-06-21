package com.agilesync.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class TrelloCardDTO implements CardDTO {
	private String id;
	private String name;
	private List<TrelloLabelDTO> labels;
}