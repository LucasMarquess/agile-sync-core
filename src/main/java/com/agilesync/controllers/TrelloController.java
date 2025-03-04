package com.agilesync.controllers;

import com.agilesync.domain.dto.TrelloBoardDTO;
import com.agilesync.domain.dto.TrelloListDTO;
import com.agilesync.domain.dto.TrelloMappingDTO;
import com.agilesync.domain.dto.TrelloSettingsDTO;
import com.agilesync.service.TrelloIntegrationService;
import com.agilesync.service.TrelloMappingService;
import com.agilesync.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("integration/trello")
@RequiredArgsConstructor
public class TrelloController {

	private final TrelloIntegrationService trelloService;
	private final TrelloMappingService trelloMappingService;

	@GetMapping("/boards")
	public ResponseEntity<List<TrelloBoardDTO>> getUserBoards() {
		List<TrelloBoardDTO> response = trelloService.getBoards();

		return ResponseEntity.ok(ObjectUtils.isNotNullOrEmpty(response) ? response : Collections.emptyList());
	}

	@GetMapping("/{boardId}/lists")
	public ResponseEntity<List<TrelloListDTO>> getListsByBoardId(@PathVariable String boardId) {
		List<TrelloListDTO> response = trelloService.getListsOfBoard(boardId);

		return ResponseEntity.ok(ObjectUtils.isNotNullOrEmpty(response) ? response : Collections.emptyList());
	}

	@PostMapping("/save")
	public ResponseEntity<?> saveIntegrationTrello(@RequestBody TrelloSettingsDTO dto) {
		trelloService.save(dto);
		return ResponseEntity.ok().build();
	}

	@GetMapping("")
	public ResponseEntity<TrelloSettingsDTO> getIntegrationTrelloUser() {
		var response = trelloService.getByUser();

		return ResponseEntity.ok(response);
	}

	@PostMapping("/mappings")
	public ResponseEntity<?> createTrelloMapping(@RequestBody List<TrelloMappingDTO> dtos) {
		trelloMappingService.saveTrelloMapping(dtos);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{trelloSettingsId}/mappings")
	public ResponseEntity<List<TrelloMappingDTO>> getListsByBoardId(@PathVariable Long trelloSettingsId) {
		var response = trelloMappingService.findByTrelloSettings(trelloSettingsId);

		return ResponseEntity.ok(ObjectUtils.isNotNullOrEmpty(response) ? response : Collections.emptyList());
	}
}
