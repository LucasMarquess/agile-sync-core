package com.agilesync.service;

import com.agilesync.client.TrelloClient;
import com.agilesync.domain.dto.*;
import com.agilesync.domain.entity.TrelloSettings;
import com.agilesync.domain.entity.UserIntegrationsSettings;
import com.agilesync.domain.enumeration.ScrumTrelloEnum;
import com.agilesync.exceptions.BadRequestException;
import com.agilesync.repository.TrelloSettingsRepository;
import com.agilesync.repository.UserIntegrationsSettingsRepository;
import com.agilesync.utils.ObjectUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrelloIntegrationService {

	private static final double SPRINT_DAYS = 15;
	@Value("${api.trello.key}")
	private String apiKey;

	private final UserIntegrationsSettingsRepository userIntegrationsSettingsRepository;
	private final TrelloSettingsRepository trelloSettingsRepository;
	private final TrelloMappingService trelloMappingService;
	private final AuthorizationService authorizationService;
	private final CfdPatternAnalyzerService cfdPatternAnalyzer;
	private final TrelloClient trelloClient;
	private final ModelMapper modelMapper;

	@Transactional
	public TrelloSettingsDTO save(TrelloSettingsDTO trelloSettingsDTO) {
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

		return modelMapper.map(userIntegrations, TrelloSettingsDTO.class);
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

	public List<TrelloLabelDTO> getLabelsBoardByUser() {
		var settings = getByUser();
		var labels = trelloClient.getBoardLabels(settings.getBoardId(), apiKey, settings.getToken(), "id,name");

		if (ObjectUtils.isNotNullOrEmpty(labels)) {
			return labels.stream()
					.sorted(Comparator.comparing(TrelloLabelDTO::getName))
					.toList();
		} else {
			return Collections.emptyList();
		}
	}

	public MetricsDTO generateMetrics(String initialPeriod, String finalPeriod, boolean isReport) {
		if (initialPeriod == null || finalPeriod == null) {
			throw new BadRequestException("Os períodos inicial e final devem ser informados.");
		}

		var labels = this.getLabelsBoardByUser();

		TrelloLabelDTO initialLabel = labels.stream()
				.filter(label -> label.getName().equalsIgnoreCase(initialPeriod))
				.findFirst()
				.orElseThrow(() -> new BadRequestException("Período inicial não encontrado: " + initialPeriod));

		TrelloLabelDTO finalLabel = labels.stream()
				.filter(label -> label.getName().equalsIgnoreCase(finalPeriod))
				.findFirst()
				.orElseThrow(() -> new BadRequestException("Período final não encontrado: " + finalPeriod));

		int sprintInicial = extractSprintNumber(initialLabel.getName());
		int sprintFinal = extractSprintNumber(finalLabel.getName());

		if (sprintInicial > sprintFinal) {
			throw new BadRequestException("O período inicial não pode ser maior que o período final.");
		}

		List<TrelloLabelDTO> selectedPeriods = labels.stream()
				.filter(label -> {
					int sprintNumber = extractSprintNumber(label.getName());
					return sprintNumber >= sprintInicial && sprintNumber <= sprintFinal;
				})
				.sorted(Comparator.comparingInt(label -> extractSprintNumber(label.getName())))
				.toList();

		var metrics = processMetricsCfd(selectedPeriods);
		metrics.setAnalysis(cfdPatternAnalyzer.analyzePatterns(metrics.getSprintCfdData(), isReport));
		return metrics;
	}

	private int extractSprintNumber(String sprintName) {
		if (sprintName == null || !sprintName.matches(".*\\d+.*")) {
			throw new BadRequestException("Nome da sprint inválido: " + sprintName);
		}

		String number = sprintName.replaceAll("\\D+", "");
		return Integer.parseInt(number);
	}

	private MetricsDTO processMetricsCfd(List<TrelloLabelDTO> selectedPeriods) {
		var integration = this.getByUser();
		var mappings = this.trelloMappingService.findByTrelloSettings(integration.getId());

		var cfdData = new ArrayList<CfdDataDTO>();

		var cumulativeCountsByStage = new HashMap<ScrumTrelloEnum, HashMap<String, Integer>>();

		for (var map : mappings) {
			var listId = map.getListId();
			var stage = map.getReferent();

			var cards = trelloClient.getCardsFromList(listId, apiKey, integration.getToken(), "id,name,labels");

			var sprintCounts = new HashMap<String, Integer>();

			for (var card : cards) {
				for (var label : card.getLabels()) {
					boolean belongsToSprint = selectedPeriods.stream()
							.anyMatch(period -> period.getId().equals(label.getId()));

					if (belongsToSprint) {
						String sprintName = label.getName();
						sprintCounts.put(sprintName, sprintCounts.getOrDefault(sprintName, 0) + 1);
					}
				}
			}

			var cumulativeCounts = cumulativeCountsByStage.computeIfAbsent(stage, k -> new HashMap<>());
			int cumulativeTotal = 0;

			for (var period : selectedPeriods) {
				String sprintName = period.getName();
				int currentCount = sprintCounts.getOrDefault(sprintName, 0);

				cumulativeTotal += currentCount;
				cumulativeCounts.put(sprintName, cumulativeCounts.getOrDefault(sprintName, 0) + cumulativeTotal);

				cfdData.add(CfdDataDTO.builder()
						.stage(stage)
						.quantityTotal(cumulativeCounts.get(sprintName))
						.quantityCards(currentCount)
						.sprint(sprintName)
						.build());
			}
		}

		var metrics = MetricsDTO.builder()
				.sprintCfdData(convertToSprintCfdDataDTO(cfdData))
				.build();
		metrics.setVelocity(this.calculateVelocity(metrics.getSprintCfdData()));
		return metrics;
	}

	private List<SprintCfdDataDTO> convertToSprintCfdDataDTO(List<CfdDataDTO> cfdDataList) {
		Map<String, SprintCfdDataDTO> sprintMap = new HashMap<>();

		Map<String, Integer> throughputBySprint = calculateThroughputBySprint(cfdDataList);
		var wipsBySprints = calculateWipBySprint(cfdDataList);

		for (CfdDataDTO data : cfdDataList) {
			String sprintName = data.getSprint();
			int sprintNumber = extractSprintNumber(sprintName);
			int throughput = throughputBySprint.getOrDefault(sprintName, 0);
			var wip = wipsBySprints.get(sprintName);
			BigDecimal cycleTime = calculateCycleTime(throughput);
			BigDecimal leadTime = calculateLeadTime(wip, throughput);

			sprintMap.computeIfAbsent(sprintName, key -> SprintCfdDataDTO.builder()
					.sprintNumber(sprintNumber)
					.cfdDatas(new ArrayList<>())
					.wipsByStage(wip)
					.throughput(throughput)
					.leadTime(leadTime)
					.cycleTime(cycleTime)
					.build()
			).getCfdDatas().add(data);
		}

		return sprintMap.values().stream()
				.sorted(Comparator.comparing(SprintCfdDataDTO::getSprintNumber))
				.toList();
	}

	private Map<String, Integer> calculateThroughputBySprint(List<CfdDataDTO> cfdDataList) {
		return cfdDataList.stream()
				.filter(data -> data.getStage() == ScrumTrelloEnum.PRONTO)
				.collect(Collectors.groupingBy(
						CfdDataDTO::getSprint,
						Collectors.summingInt(CfdDataDTO::getQuantityCards)
				));
	}

	private Map<String, List<WipDTO>> calculateWipBySprint(List<CfdDataDTO> cfdDataList) {
		return cfdDataList.stream()
				.filter(data -> data.getStage() != ScrumTrelloEnum.PRONTO && data.getStage() != ScrumTrelloEnum.BACKLOG)
				.collect(Collectors.groupingBy(
						CfdDataDTO::getSprint,
						Collectors.groupingBy(
								CfdDataDTO::getStage,
								Collectors.summingInt(CfdDataDTO::getQuantityCards)
						)
				))
				.entrySet().stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						entry -> entry.getValue().entrySet().stream()
								.map(stageEntry -> new WipDTO(stageEntry.getKey(), stageEntry.getValue()))
								.collect(Collectors.toList())
				));
	}

	private BigDecimal calculateVelocity(List<SprintCfdDataDTO> cfdDataList) {
		if (ObjectUtils.isNullOrEmpty(cfdDataList)) {
			return BigDecimal.ZERO;
		}

		int total = cfdDataList.stream()
				.mapToInt(SprintCfdDataDTO::getThroughput)
				.sum();

		return ObjectUtils.truncateToTwoDecimals(BigDecimal.valueOf(total)
				.divide(BigDecimal.valueOf(cfdDataList.size()), 10, RoundingMode.DOWN));
	}

	private BigDecimal calculateCycleTime(int throughput) {
		if (throughput <= 0) {
			return BigDecimal.ZERO;
		}

		return ObjectUtils.truncateToTwoDecimals(BigDecimal.valueOf(SPRINT_DAYS)
				.divide(BigDecimal.valueOf(throughput), 10, RoundingMode.DOWN));
	}

	private BigDecimal calculateLeadTime(List<WipDTO> wipList, int throughput) {
		if (throughput <= 0) {
			return BigDecimal.ZERO;
		}

		int totalWip = wipList.stream()
				.mapToInt(WipDTO::getQuantity)
				.sum();

		return ObjectUtils.truncateToTwoDecimals(BigDecimal.valueOf(totalWip)
				.divide(BigDecimal.valueOf(throughput), 10, RoundingMode.DOWN));
	}
}
