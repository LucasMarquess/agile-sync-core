package com.agilesync.service;

import com.agilesync.repository.TrelloSettingsRepository;
import org.springframework.stereotype.Service;

@Service
public class IntegrationsService {

	TrelloSettingsRepository trelloSettingsRepository;

	public boolean verifyIfUserHasAnyIntegration() {
		return true;
	}
}
