package com.agilesync.service;

import com.agilesync.domain.dto.CfdDataDTO;
import com.agilesync.domain.dto.SprintCfdDataDTO;
import com.agilesync.domain.dto.WipDTO;
import com.agilesync.domain.enumeration.ScrumTrelloEnum;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CfdPatternAnalyzerService {

	public static String analyzePatterns(List<SprintCfdDataDTO> sprints) {
		StringBuilder report = new StringBuilder();

		Map<String, List<Integer>> patterns = new LinkedHashMap<>();

		for (SprintCfdDataDTO sprint : sprints) {
			int sprintNumber = sprint.getSprintNumber();
			int totalWip = sprint.getWipsByStage().stream().mapToInt(WipDTO::getQuantity).sum();
			int throughput = sprint.getThroughput();
			BigDecimal leadTime = sprint.getLeadTime();
			BigDecimal cycleTime = sprint.getCycleTime();

			if (isGradientDifference(totalWip, throughput)) {
				patterns.computeIfAbsent("Diferença no Gradiente", k -> new ArrayList<>()).add(sprintNumber);
			}
			if (isFlatLines(throughput)) {
				patterns.computeIfAbsent("Linhas Planas", k -> new ArrayList<>()).add(sprintNumber);
			}
			if (isSCurve(leadTime, cycleTime, totalWip)) {
				patterns.computeIfAbsent("Curva-S", k -> new ArrayList<>()).add(sprintNumber);
			}
			if (isBulgingSpacing(totalWip, throughput)) {
				patterns.computeIfAbsent("Espaçamentos Protuberantes", k -> new ArrayList<>()).add(sprintNumber);
			}
			if (isStairSteps(sprint)) {
				patterns.computeIfAbsent("Degraus da Escada", k -> new ArrayList<>()).add(sprintNumber);
			}
			if (isDisappearingSpacing(sprint)) {
				patterns.computeIfAbsent("Espaçamentos Desaparecendo", k -> new ArrayList<>()).add(sprintNumber);
			}
		}

		patterns.forEach((pattern, sprintsList) -> {
			report.append(pattern).append(" identificado na(s) sprint(s): ")
					.append(sprintsList.stream().map(String::valueOf).collect(Collectors.joining(", ")))
					.append(".\n");
		});

		return report.toString();
	}

	private static boolean isGradientDifference(int totalWip, int throughput) {
		return throughput < totalWip * 0.8; // Indicador de aumento do WIP
	}

	private static boolean isFlatLines(int throughput) {
		return throughput == 0; // Indica estagnação do fluxo
	}

	private static boolean isSCurve(BigDecimal leadTime, BigDecimal cycleTime, int totalWip) {
		return leadTime.compareTo(cycleTime) > 0 && totalWip > 5; // Indica flutuação na produtividade
	}

	private static boolean isBulgingSpacing(int totalWip, int throughput) {
		return totalWip > throughput * 2 && throughput > 0; // Sobrecarga desproporcional
	}

	private static boolean isStairSteps(SprintCfdDataDTO sprint) {
		return sprint.getCfdDatas().stream()
				.collect(Collectors.groupingBy(CfdDataDTO::getSprint, Collectors.summingInt(CfdDataDTO::getQuantityCards)))
				.values().stream().anyMatch(q -> q > sprint.getThroughput() / 3); // Entregas em lotes
	}

	private static boolean isDisappearingSpacing(SprintCfdDataDTO sprint) {
		Set<ScrumTrelloEnum> stages = sprint.getCfdDatas().stream().map(CfdDataDTO::getStage).collect(Collectors.toSet());
		return !stages.contains(ScrumTrelloEnum.DESENVOLVIMENTO) && stages.contains(ScrumTrelloEnum.PRONTO);
	}
}

