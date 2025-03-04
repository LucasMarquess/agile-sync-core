package com.agilesync.service;

import com.agilesync.client.TrelloClient;
import com.agilesync.domain.dto.TrelloBoardDTO;
import com.agilesync.domain.dto.TrelloListDTO;
import com.agilesync.domain.dto.TrelloSettingsDTO;
import com.agilesync.domain.entity.TrelloSettings;
import com.agilesync.domain.entity.UserIntegrationsSettings;
import com.agilesync.repository.TrelloSettingsRepository;
import com.agilesync.repository.UserIntegrationsSettingsRepository;
import com.agilesync.utils.ObjectUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrelloIntegrationService {

	@Value("${api.trello.key}")
	private String apiKey;

	private final UserIntegrationsSettingsRepository userIntegrationsSettingsRepository;
	private final TrelloSettingsRepository trelloSettingsRepository;
	private final AuthorizationService authorizationService;
	private final TrelloClient trelloClient;
	private final ModelMapper modelMapper;

	@Transactional
	public void save(TrelloSettingsDTO trelloSettingsDTO) {
		var user = authorizationService.getCurrentUser();
		var trelloSetting = modelMapper.map(trelloSettingsDTO, TrelloSettings.class);

		UserIntegrationsSettings userIntegrations = userIntegrationsSettingsRepository.findByUserId(user.getId())
				.orElse(new UserIntegrationsSettings());
		userIntegrations.setUser(user);
		if (ObjectUtils.isNullOrEmpty(userIntegrations.getTrelloSettings())) {
			userIntegrations.setTrelloSettings(trelloSetting);
		} else {
			userIntegrations.getTrelloSettings().setToken(trelloSettingsDTO.getToken());
			userIntegrations.getTrelloSettings().setBoardId(trelloSettingsDTO.getBoardId());
		}

		userIntegrationsSettingsRepository.save(userIntegrations);
	}

	public TrelloSettingsDTO getByUser() {
		var user = authorizationService.getCurrentUser();
		if (ObjectUtils.isNotNullOrEmpty(user)) {
			var entity = trelloSettingsRepository.findByUserId(user.getId());
			return ObjectUtils.isNotNullOrEmpty(entity) ? modelMapper.map(entity, TrelloSettingsDTO.class) : null;
		} else {
			return null;
		}
	}

	public List<TrelloBoardDTO> getBoards() {
		var settings = getByUser();
		var fields = "id,name";

		return trelloClient.getBoards(apiKey, settings.getToken(), fields);
	}

	public List<TrelloListDTO> getListsOfBoard(String boardId) {
		var settings = getByUser();
		var fields = "id,name";

		return trelloClient.getBoardLists(boardId, apiKey, settings.getToken(), fields);
	}
}
