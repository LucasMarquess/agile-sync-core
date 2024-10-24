package com.agilesync.domain.entity;

import com.agilesync.domain.enumeration.IntegrationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name="integrations")
@MappedSuperclass
public abstract class Integrations {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "integrations_id_seq")
	@SequenceGenerator(name = "integrations_id_seq", sequenceName = "integrations_id_seq",  allocationSize=1)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private IntegrationType integrationType;

	public Integrations(User user, IntegrationType integrationType) {
		this.user = user;
		this.integrationType = integrationType;
	}
}
