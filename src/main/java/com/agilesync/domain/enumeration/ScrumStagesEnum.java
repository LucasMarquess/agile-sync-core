package com.agilesync.domain.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ScrumStagesEnum {
	BACKLOG("Backlog"),
	DESENVOLVIMENTO("Desenvolvimento"),
	TESTES("Teste"),
	PRONTO("Pronto");

	private final String description;

	ScrumStagesEnum(String description) {
		this.description = description;
	}
	
	@JsonValue
	public String getDescription() {
		return description;
	}

	@JsonCreator
	public static ScrumStagesEnum fromDescription(String description) {
		for (ScrumStagesEnum value : ScrumStagesEnum.values()) {
			if (value.description.equalsIgnoreCase(description)) {
				return value;
			}
		}
		throw new IllegalArgumentException("Nenhum enum encontrado para a descrição: " + description);
	}
}
