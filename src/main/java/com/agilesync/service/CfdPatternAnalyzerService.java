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

	public static String analyzePatterns(List<SprintCfdDataDTO> sprints, boolean isReport) {
		StringBuilder report = new StringBuilder();
		Map<String, List<Integer>> patterns = new LinkedHashMap<>();

		for (Integer i = 0; i < sprints.size(); i++) {
			var sprint = sprints.get(i);
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
			if (isStairSteps(sprints, i)) {
				patterns.computeIfAbsent("Degraus da Escada", k -> new ArrayList<>()).add(sprintNumber);
			}
			if (isDisappearingSpacing(sprint)) {
				patterns.computeIfAbsent("Espaçamentos Desaparecendo", k -> new ArrayList<>()).add(sprintNumber);
			}
		}

		patterns.forEach((pattern, sprintsList) -> {
			report.append("<b> i. ").append(pattern).append("</b> - identificado na(s) sprint(s): ")
					.append(sprintsList.stream().map(String::valueOf).collect(Collectors.joining(", ")))
					.append(".<br>");
			report.append("	<b>ii. Explicação: </b>");
			switch (pattern) {
			case "Diferença no Gradiente":
				report.append("Trabalho em progresso (WIP) vs. total de itens prontos(throughput). WIP excede 80% do throughput, indicando aumento no trabalho em progresso.");
				break;
			case "Linhas Planas":
				report.append("Total de itens prontos(throughput) igual a zero, indicando estagnação do fluxo de trabalho.");
				break;
			case "Curva-S":
				report.append("Lead time e cycle time comparados. Se lead time for maior e trabalho em progresso (WIP) for superior a 5, há flutuação na produtividade.");
				break;
			case "Espaçamentos Protuberantes":
				report.append("Trabalho em progresso (WIP) total superior ao dobro do total de itens prontos (throughput), indicando sobrecarga desproporcional.");
				break;
			case "Degraus da Escada":
				report.append("Foi identificado comparando o total de itens prontos (throughput) de cada sprint com a anterior. ")
						.append("Variações bruscas (+50% ou -50%) entre sprints podem indicar entregas em lotes, ")
						.append("gargalos ou acúmulo de trabalho.\n");
				break;
			case "Espaçamentos Desaparecendo":
				report.append("Ausência da fase 'Desenvolvimento' e presença da fase 'Pronto', indicando possíveis gaps ocultos no fluxo.");
				break;
			}
			report.append("<br>");
			if (isReport) {
				report.append("<br>");
			} else {
				report.append("<hr>");
			}
		});

		return report.toString();
	}

	private static boolean isGradientDifference(int totalWip, int throughput) {
		return throughput < totalWip * 0.8;
	}

	private static boolean isFlatLines(int throughput) {
		return throughput == 0;
	}

	private static boolean isSCurve(BigDecimal leadTime, BigDecimal cycleTime, int totalWip) {
		return leadTime.compareTo(cycleTime) > 0 && totalWip > 5;
	}

	private static boolean isBulgingSpacing(int totalWip, int throughput) {
		return totalWip > throughput * 2 && throughput > 0;
	}

	private static boolean isStairSteps(List<SprintCfdDataDTO> sprints, int currentIndex) {
		if (currentIndex == 0) {
			return false;
		}

		int currentThroughput = sprints.get(currentIndex).getThroughput();
		int previousThroughput = sprints.get(currentIndex - 1).getThroughput();

		return previousThroughput > 0 && (currentThroughput > previousThroughput * 1.5 || previousThroughput > currentThroughput * 1.5);
	}

	private static boolean isDisappearingSpacing(SprintCfdDataDTO sprint) {
		Set<ScrumTrelloEnum> stages = sprint.getCfdDatas().stream().map(CfdDataDTO::getStage).collect(Collectors.toSet());
		return !stages.contains(ScrumTrelloEnum.DESENVOLVIMENTO) && stages.contains(ScrumTrelloEnum.PRONTO);
	}
}
