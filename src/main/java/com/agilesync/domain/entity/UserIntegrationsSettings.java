package com.agilesync.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_integrations_settings")
@Getter
@Setter
public class UserIntegrationsSettings {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_settings_seq")
	@SequenceGenerator(name = "user_settings_seq", sequenceName = "user_settings_seq", allocationSize = 1)
	private Long id;

	@OneToOne(optional = false)
	@JoinColumn(name = "user_id", referencedColumnName = "id", unique = true, nullable = false)
	private User user;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "trello_settings_id", referencedColumnName = "id")
	private TrelloSettings trelloSettings;
}