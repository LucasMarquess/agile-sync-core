package com.agilesync.service;

import com.agilesync.client.AgileToolClient;
import com.agilesync.domain.dto.*;
import com.agilesync.domain.entity.TrelloSettings;
import com.agilesync.domain.enumeration.ScrumTrelloEnum;
import com.agilesync.exceptions.BadRequestException;
import com.agilesync.repository.TrelloSettingsRepository;
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

	private final TrelloSettingsRepository trelloSettingsRepository;
	private final TrelloMappingService trelloMappingService;
	private final AuthorizationService authorizationService;
	private final CfdPatternAnalyzerService cfdPatternAnalyzer;
	private final AgileToolClient agileToolClient;
	private final ModelMapper modelMapper;

	private TrelloSettings resolveOwnedIntegration(Long integrationId) {
		if (integrationId == null) {
			throw new BadRequestException("O identificador da integração deve ser informado.");
		}
		var user = authorizationService.getCurrentUser();
		return trelloSettingsRepository.findByIdAndUserId(integrationId, user.getId())
				.orElseThrow(() -> new BadRequestException("Integração não encontrada ou não pertence ao usuário."));
	}

	private AgileToolClient resolveClient(TrelloSettings integration) {
		return agileToolClient;
	}

	@Transactional
	public TrelloSettingsDTO save(TrelloSettingsDTO trelloSettingsDTO) {
		var user = authorizationService.getCurrentUser();
		TrelloSettings entity;

		if (trelloSettingsDTO.getId() != null) {
			entity = resolveOwnedIntegration(trelloSettingsDTO.getId());
			entity.setToken(trelloSettingsDTO.getToken());
			entity.setBoardId(trelloSettingsDTO.getBoardId());
			if (ObjectUtils.isNotNullOrEmpty(trelloSettingsDTO.getName())) {
				entity.setName(trelloSettingsDTO.getName());
			}
		} else {
			boolean isDuplicate = ObjectUtils.isNotNullOrEmpty(trelloSettingsDTO.getBoardId())
					&& trelloSettingsRepository.existsByUserIdAndBoardIdAndToken(
							user.getId(), trelloSettingsDTO.getBoardId(), trelloSettingsDTO.getToken());
			if (isDuplicate) {
				throw new BadRequestException("Já existe uma integração com este mesmo board e token.");
			}

			entity = modelMapper.map(trelloSettingsDTO, TrelloSettings.class);
			entity.setId(null);
			entity.setUser(user);
			if (ObjectUtils.isNullOrEmpty(entity.getName())) {
				entity.setName("Integração Trello");
			}
		}

		trelloSettingsRepository.save(entity);
		return modelMapper.map(entity, TrelloSettingsDTO.class);
	}

	public TrelloSettingsDTO getById(Long integrationId) {
		var entity = resolveOwnedIntegration(integrationId);
		return modelMapper.map(entity, TrelloSettingsDTO.class);
	}

	public List<? extends BoardDTO> getBoards(Long integrationId) {
		var integration = resolveOwnedIntegration(integrationId);
		var fields = "id,name";

		return resolveClient(integration).getBoards(apiKey, integration.getToken(), fields);
	}

	public List<? extends ListDTO> getListsOfBoard(Long integrationId, String boardId) {
		var integration = resolveOwnedIntegration(integrationId);
		var fields = "id,name";

		return resolveClient(integration).getBoardLists(boardId, apiKey, integration.getToken(), fields);
	}

	public List<? extends LabelDTO> getLabelsBoardByUser(Long integrationId) {
		var integration = resolveOwnedIntegration(integrationId);
		var labels = resolveClient(integration)
				.getBoardLabels(integration.getBoardId(), apiKey, integration.getToken(), "id,name");

		if (ObjectUtils.isNotNullOrEmpty(labels)) {
			return labels.stream()
					.sorted(Comparator.comparing(LabelDTO::getName))
					.toList();
		} else {
			return Collections.emptyList();
		}
	}

	public MetricsDTO generateMetrics(Long integrationId, String initialPeriod, String finalPeriod, boolean isReport) {
		if (initialPeriod == null || finalPeriod == null) {
			throw new BadRequestException("Os períodos inicial e final devem ser informados.");
		}

		var labels = this.getLabelsBoardByUser(integrationId);

		LabelDTO initialLabel = labels.stream()
				.filter(label -> label.getName().equalsIgnoreCase(initialPeriod))
				.findFirst()
				.orElseThrow(() -> new BadRequestException("Período inicial não encontrado: " + initialPeriod));

		LabelDTO finalLabel = labels.stream()
				.filter(label -> label.getName().equalsIgnoreCase(finalPeriod))
				.findFirst()
				.orElseThrow(() -> new BadRequestException("Período final não encontrado: " + finalPeriod));

		int sprintInicial = extractSprintNumber(initialLabel.getName());
		int sprintFinal = extractSprintNumber(finalLabel.getName());

		if (sprintInicial > sprintFinal) {
			throw new BadRequestException("O período inicial não pode ser maior que o período final.");
		}

		List<? extends LabelDTO> selectedPeriods = labels.stream()
				.filter(label -> {
					int sprintNumber = extractSprintNumber(label.getName());
					return sprintNumber >= sprintInicial && sprintNumber <= sprintFinal;
				})
				.sorted(Comparator.comparingInt(label -> extractSprintNumber(label.getName())))
				.toList();

		var metrics = processMetricsCfd(integrationId, selectedPeriods);
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

	private MetricsDTO processMetricsCfd(Long integrationId, List<? extends LabelDTO> selectedPeriods) {
		var integration = resolveOwnedIntegration(integrationId);
		var client = resolveClient(integration);
		var mappings = this.trelloMappingService.findByTrelloSettings(integration.getId());

		var cfdData = new ArrayList<CfdDataDTO>();

		var cumulativeCountsByStage = new HashMap<ScrumTrelloEnum, HashMap<String, Integer>>();

		for (var map : mappings) {
			var listId = map.getListId();
			var stage = map.getReferent();

			var cards = client.getCardsFromList(listId, apiKey, integration.getToken(), "id,name,labels");

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
		Map<String, Integer> backlogWipBySprint = calculateBacklogWipBySprint(cfdDataList);

		for (CfdDataDTO data : cfdDataList) {
			String sprintName = data.getSprint();
			int sprintNumber = extractSprintNumber(sprintName);
			int throughput = throughputBySprint.getOrDefault(sprintName, 0);
			var wip = wipsBySprints.getOrDefault(sprintName, Collections.emptyList());
			int wipEmProgresso = wip.stream().mapToInt(WipDTO::getQuantity).sum();
			int wipBacklog = backlogWipBySprint.getOrDefault(sprintName, 0);
			BigDecimal cycleTime = calculateCycleTime(wipEmProgresso, throughput);
			BigDecimal leadTime = calculateLeadTime(wipEmProgresso, wipBacklog, throughput);
			BigDecimal flowEfficiency = calculateFlowEfficiency(wipEmProgresso, wipBacklog);
			int netFlow = calculateNetFlow(wipBacklog, throughput);

			sprintMap.computeIfAbsent(sprintName, key -> SprintCfdDataDTO.builder()
					.sprintNumber(sprintNumber)
					.cfdDatas(new ArrayList<>())
					.wipsByStage(wip)
					.throughput(throughput)
					.leadTime(leadTime)
					.cycleTime(cycleTime)
					.flowEfficiency(flowEfficiency)
					.netFlow(netFlow)
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

	private Map<String, Integer> calculateBacklogWipBySprint(List<CfdDataDTO> cfdDataList) {
		return cfdDataList.stream()
				.filter(data -> data.getStage() == ScrumTrelloEnum.BACKLOG)
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

	private BigDecimal calculateCycleTime(int wipEmProgresso, int throughput) {
		if (throughput <= 0) {
			return BigDecimal.ZERO;
		}

		return ObjectUtils.truncateToTwoDecimals(BigDecimal.valueOf(wipEmProgresso)
				.multiply(BigDecimal.valueOf(SPRINT_DAYS))
				.divide(BigDecimal.valueOf(throughput), 10, RoundingMode.DOWN));
	}

	private BigDecimal calculateLeadTime(int wipEmProgresso, int wipBacklog, int throughput) {
		if (throughput <= 0) {
			return BigDecimal.ZERO;
		}

		int wipTotal = wipEmProgresso + wipBacklog;

		return ObjectUtils.truncateToTwoDecimals(BigDecimal.valueOf(wipTotal)
				.multiply(BigDecimal.valueOf(SPRINT_DAYS))
				.divide(BigDecimal.valueOf(throughput), 10, RoundingMode.DOWN));
	}

	private BigDecimal calculateFlowEfficiency(int wipEmProgresso, int wipBacklog) {
		int wipTotal = wipEmProgresso + wipBacklog;

		if (wipTotal <= 0) {
			return BigDecimal.ZERO;
		}

		return ObjectUtils.truncateToTwoDecimals(BigDecimal.valueOf(wipEmProgresso)
				.multiply(BigDecimal.valueOf(100))
				.divide(BigDecimal.valueOf(wipTotal), 10, RoundingMode.DOWN));
	}

	private int calculateNetFlow(int wipBacklog, int throughput) {
		return wipBacklog - throughput;
	}
}
