package com.agilesync.domain.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ScrumTrelloEnum {
	BACKLOG("Backlog"),
	DESENVOLVIMENTO("Desenvolvimento"),
	TESTES("Teste"),
	PRONTO("Pronto");

	private final String description;

	ScrumTrelloEnum(String description) {
		this.description = description;
	}

	@JsonValue
	public String getDescription() {
		return description;
	}

	@JsonCreator
	public static ScrumTrelloEnum fromDescription(String description) {
		for (ScrumTrelloEnum value : ScrumTrelloEnum.values()) {
			if (value.description.equalsIgnoreCase(description)) {
				return value;
			}
		}
		throw new IllegalArgumentException("Nenhum enum encontrado para a descrição: " + description);
	}
}
