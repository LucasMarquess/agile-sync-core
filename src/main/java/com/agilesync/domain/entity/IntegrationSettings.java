package com.agilesync.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class IntegrationSettings {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "integration_seq")
	@SequenceGenerator(name = "integration_seq", sequenceName = "integration_seq", allocationSize = 1)
	private Long id;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(nullable = false)
	private String token;

	@Column(nullable = false)
	private String key;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
