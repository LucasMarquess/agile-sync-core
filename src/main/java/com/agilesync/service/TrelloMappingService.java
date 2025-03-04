package com.agilesync.service;

import com.agilesync.domain.dto.TrelloMappingDTO;
import com.agilesync.domain.entity.TrelloMapping;
import com.agilesync.domain.entity.TrelloSettings;
import com.agilesync.domain.enumeration.ScrumTrelloEnum;
import com.agilesync.repository.TrelloMappingRepository;
import com.agilesync.repository.TrelloSettingsRepository;
import com.agilesync.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrelloMappingService {

	private final TrelloMappingRepository trelloMappingRepository;
	private final TrelloSettingsRepository trelloSettingsRepository;

	@Transactional
	public void saveTrelloMapping(List<TrelloMappingDTO> dtos) {
		if (ObjectUtils.isNullOrEmpty(dtos)) {
			return;
		}

		Long trelloSettingId = dtos.get(0).getTrelloSettingId();
		TrelloSettings trelloSettings = trelloSettingsRepository.findById(trelloSettingId)
				.orElseThrow(() -> new IllegalArgumentException("TrelloSettings não encontrado"));

		List<ScrumTrelloEnum> referentsInDTO = dtos.stream()
				.map(TrelloMappingDTO::getReferent)
				.collect(Collectors.toList());

		List<TrelloMapping> existingMappings = trelloMappingRepository.getTrelloMappingsByTrelloSettingsId(trelloSettingId);

		existingMappings.stream()
				.filter(mapping -> !referentsInDTO.contains(mapping.getReferent()))
				.forEach(trelloMappingRepository::delete);

		for (TrelloMappingDTO dto : dtos) {
			TrelloMapping entity = findOrCreateEntity(dto, trelloSettings);
			if (ObjectUtils.isNotNullOrEmpty(entity)) {
				trelloMappingRepository.save(entity);
			}
		}
	}

	public List<TrelloMappingDTO> findByTrelloSettings(Long trelloSettingId) {
		var list = trelloMappingRepository.getTrelloMappingsByTrelloSettingsId(trelloSettingId);
		return ObjectUtils.isNotNullOrEmpty(list) ?
				list.stream().map(TrelloMappingDTO::mapToDTO).collect(Collectors.toList()) : Collections.emptyList();
	}

	private TrelloMapping findOrCreateEntity(TrelloMappingDTO dto, TrelloSettings trelloSettings) {
		Optional<TrelloMapping> existingReferent = Optional.ofNullable(trelloMappingRepository
				.findByTrelloSettingsIdAndReferent(trelloSettings.getId(), dto.getReferent()));

		if (existingReferent.isPresent()) {
			var entity = existingReferent.get();
			entity.setListName(dto.getListName());
			return entity;
		} else {
			return TrelloMappingDTO.mapToEntity(dto, trelloSettings);
		}
	}
}