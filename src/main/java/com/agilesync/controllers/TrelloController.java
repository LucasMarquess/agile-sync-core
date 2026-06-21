package com.agilesync.controllers;

import com.agilesync.domain.dto.*;
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
	public ResponseEntity<List<? extends BoardDTO>> getUserBoards(@RequestParam Long integrationId) {
		List<? extends BoardDTO> response = trelloService.getBoards(integrationId);

		return ResponseEntity.ok(ObjectUtils.isNotNullOrEmpty(response) ? response : Collections.emptyList());
	}

	@GetMapping("/{boardId}/lists")
	public ResponseEntity<List<? extends ListDTO>> getListsByBoardId(
			@PathVariable String boardId, @RequestParam Long integrationId) {
		List<? extends ListDTO> response = trelloService.getListsOfBoard(integrationId, boardId);

		return ResponseEntity.ok(ObjectUtils.isNotNullOrEmpty(response) ? response : Collections.emptyList());
	}

	@PostMapping("/save")
	public ResponseEntity<?> saveIntegrationTrello(@RequestBody TrelloSettingsDTO dto) {
		return ResponseEntity.ok(trelloService.save(dto));
	}

	@GetMapping("/{integrationId}")
	public ResponseEntity<TrelloSettingsDTO> getIntegrationTrelloUser(@PathVariable Long integrationId) {
		var response = trelloService.getById(integrationId);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/mappings")
	public ResponseEntity<?> createTrelloMapping(@RequestBody List<TrelloMappingDTO> dtos) {
		trelloMappingService.saveTrelloMapping(dtos);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{integrationId}/mappings")
	public ResponseEntity<List<TrelloMappingDTO>> getMappingsByIntegration(@PathVariable Long integrationId) {
		var response = trelloMappingService.findByTrelloSettings(integrationId);

		return ResponseEntity.ok(ObjectUtils.isNotNullOrEmpty(response) ? response : Collections.emptyList());
	}

	@GetMapping("/labels")
	public ResponseEntity<List<? extends LabelDTO>> getLabelsBoardByUser(@RequestParam Long integrationId) {
		List<? extends LabelDTO> response = trelloService.getLabelsBoardByUser(integrationId);
		return ResponseEntity.ok(ObjectUtils.isNotNullOrEmpty(response) ? response : Collections.emptyList());
	}

	@GetMapping("/metrics")
	public ResponseEntity<?> getMetrics(
			@RequestParam Long integrationId,
			@RequestParam String initialPeriod,
			@RequestParam String finalPeriod) {
		var response = trelloService.generateMetrics(integrationId, initialPeriod, finalPeriod, false);
		return ResponseEntity.ok(ObjectUtils.isNotNullOrEmpty(response) ? response : Collections.emptyList());
	}
}
