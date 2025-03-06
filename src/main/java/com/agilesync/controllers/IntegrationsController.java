package com.agilesync.controllers;

import com.agilesync.service.IntegrationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("integrations")
@RequiredArgsConstructor
public class IntegrationsController {

	private final IntegrationsService integrationsService;

	@GetMapping("/check")
	public ResponseEntity<Boolean> checkIntegrations() {
		return ResponseEntity.ok(integrationsService.verifyIfUserHasAnyIntegration());
	}
}
