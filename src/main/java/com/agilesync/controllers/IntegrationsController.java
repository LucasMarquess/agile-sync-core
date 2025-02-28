package com.agilesync.controllers;

import com.agilesync.domain.dto.TrelloSettingsDTO;
import com.agilesync.service.TrelloIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("integrations-settings")
@RequiredArgsConstructor
public class IntegrationsController {

	private final TrelloIntegrationService trelloService;

	@PostMapping("/save/trello")
	public ResponseEntity<?> saveIntegrationTrello(@RequestBody TrelloSettingsDTO dto) {
		trelloService.save(dto);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/trello")
	public ResponseEntity<TrelloSettingsDTO> getIntegrationTrelloUser() {
		var reponse = trelloService.getByUser();

		if (reponse == null) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok(reponse);
	}
}
