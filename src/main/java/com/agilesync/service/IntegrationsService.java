package com.agilesync.service;

import com.agilesync.domain.dto.IntegrationSummaryDTO;
import com.agilesync.domain.entity.TrelloSettings;
import com.agilesync.exceptions.BadRequestException;
import com.agilesync.repository.TrelloSettingsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IntegrationsService {

	private static final String TRELLO_TYPE = "TRELLO";

	private final TrelloSettingsRepository trelloSettingsRepository;
	private final AuthorizationService authorizationService;

	public Boolean verifyIfUserHasAnyIntegration() {
		var user = authorizationService.getCurrentUser();

		return trelloSettingsRepository.verifyUserHasIntegration(user.getId());
	}

	public List<IntegrationSummaryDTO> listUserIntegrations() {
		var user = authorizationService.getCurrentUser();

		return trelloSettingsRepository.findAllByUserIdAndActiveTrue(user.getId()).stream()
				.map(this::toSummary)
				.toList();
	}

	public List<IntegrationSummaryDTO> listAllUserIntegrations() {
		var user = authorizationService.getCurrentUser();

		return trelloSettingsRepository.findAllByUserId(user.getId()).stream()
				.map(this::toSummary)
				.toList();
	}

	private IntegrationSummaryDTO toSummary(TrelloSettings integration) {
		return IntegrationSummaryDTO.builder()
				.id(integration.getId())
				.name(integration.getName())
				.type(TRELLO_TYPE)
				.fullyConfigured(trelloSettingsRepository.verifyIntegrationIsConfigured(integration.getId()))
				.active(Boolean.TRUE.equals(integration.getActive()))
				.build();
	}

	@Transactional
	public void inactivateIntegration(Long integrationId) {
		var integration = resolveOwnedIntegration(integrationId);
		integration.setActive(false);
		trelloSettingsRepository.save(integration);
	}

	@Transactional
	public void activateIntegration(Long integrationId) {
		var integration = resolveOwnedIntegration(integrationId);
		integration.setActive(true);
		trelloSettingsRepository.save(integration);
	}

	@Transactional
	public void deleteIntegration(Long integrationId) {
		var integration = resolveOwnedIntegration(integrationId);
		trelloSettingsRepository.delete(integration);
	}

	private TrelloSettings resolveOwnedIntegration(Long integrationId) {
		if (integrationId == null) {
			throw new BadRequestException("O identificador da integração deve ser informado.");
		}
		var user = authorizationService.getCurrentUser();
		return trelloSettingsRepository.findByIdAndUserId(integrationId, user.getId())
				.orElseThrow(() -> new BadRequestException("Integração não encontrada ou não pertence ao usuário."));
	}
}
