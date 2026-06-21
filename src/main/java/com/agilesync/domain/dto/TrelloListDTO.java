package com.agilesync.domain.dto;

import lombok.Data;

@Data
public class TrelloListDTO implements ListDTO {
	private String id;
	private String name;
}