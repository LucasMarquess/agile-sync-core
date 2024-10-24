package com.agilesync.domain.entity;

import com.agilesync.domain.enumeration.IntegrationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "integrations_trello", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"user_id", "integration_type"})
})
public class IntegrationsTrello extends Integrations {

	@Column(nullable = false)
	private String token;

	@Column(nullable = false)
	private String key;

	public IntegrationsTrello(User user, String token, String key) {
		super(user, IntegrationType.TRELLO);
		this.token = token;
		this.key = key;
	}
}
