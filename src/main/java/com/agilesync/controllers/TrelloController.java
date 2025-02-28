package com.agilesync.controllers;

import com.agilesync.domain.dto.TrelloBoardDTO;
import com.agilesync.domain.dto.TrelloSettingsDTO;
import com.agilesync.service.TrelloIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("integration/trello")
@RequiredArgsConstructor
public class TrelloController {

	private final TrelloIntegrationService trelloService;

	@GetMapping("/boards")
	public ResponseEntity<List<TrelloBoardDTO>> getIntegrationTrelloUser() {
		var reponse = trelloService.getBoards();

		if (reponse == null) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok(reponse);
	}
}
