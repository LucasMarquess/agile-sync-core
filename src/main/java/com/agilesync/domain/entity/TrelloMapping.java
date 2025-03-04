package com.agilesync.domain.entity;

import com.agilesync.domain.enumeration.ScrumTrelloEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "trello_mapping", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"trello_settings_id", "referent"}),
})
@Getter
@Setter
public class TrelloMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trello_mapping_seq")
	@SequenceGenerator(name = "trello_mapping_seq", sequenceName = "trello_mapping_seq", allocationSize = 1)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "referent", nullable = false)
	private ScrumTrelloEnum referent;

	@Column(name = "list_id", nullable = false)
	private String listId;

	@Column(name = "list_name", nullable = false)
	private String listName;

	@ManyToOne
	@JoinColumn(name = "trello_settings_id", referencedColumnName = "id", nullable = false)
	private TrelloSettings trelloSettings;
}
