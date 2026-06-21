package com.agilesync.controllers;

import com.agilesync.domain.dto.IntegrationSummaryDTO;
import com.agilesync.service.IntegrationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("integrations")
@RequiredArgsConstructor
public class IntegrationsController {

	private final IntegrationsService integrationsService;

	@GetMapping("/check")
	public ResponseEntity<Boolean> checkIntegrations() {
		return ResponseEntity.ok(integrationsService.verifyIfUserHasAnyIntegration());
	}

	@GetMapping("")
	public ResponseEntity<List<IntegrationSummaryDTO>> listIntegrations() {
		return ResponseEntity.ok(integrationsService.listUserIntegrations());
	}

	@GetMapping("/all")
	public ResponseEntity<List<IntegrationSummaryDTO>> listAllIntegrations() {
		return ResponseEntity.ok(integrationsService.listAllUserIntegrations());
	}

	@PatchMapping("/{integrationId}/inactivate")
	public ResponseEntity<Void> inactivateIntegration(@PathVariable Long integrationId) {
		integrationsService.inactivateIntegration(integrationId);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{integrationId}/activate")
	public ResponseEntity<Void> activateIntegration(@PathVariable Long integrationId) {
		integrationsService.activateIntegration(integrationId);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{integrationId}")
	public ResponseEntity<Void> deleteIntegration(@PathVariable Long integrationId) {
		integrationsService.deleteIntegration(integrationId);
		return ResponseEntity.noContent().build();
	}
}
