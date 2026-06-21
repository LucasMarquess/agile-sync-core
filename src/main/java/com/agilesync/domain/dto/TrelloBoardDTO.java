package com.agilesync.domain.dto;

import lombok.Data;

@Data
public class TrelloBoardDTO implements BoardDTO {
	private String id;
	private String name;
}
