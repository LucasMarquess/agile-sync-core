package com.agilesync.service;

import com.agilesync.repository.TrelloSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IntegrationsService {

	private final TrelloSettingsRepository trelloSettingsRepository;
	private final AuthorizationService authorizationService;

	public Boolean verifyIfUserHasAnyIntegration() {
		var user = authorizationService.getCurrentUser();

		return trelloSettingsRepository.verifyUserHasIntegration(user.getId());
	}
}
