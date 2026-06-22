package com.agilesync.service;

import com.agilesync.domain.dto.SprintCfdDataDTO;
import com.agilesync.domain.dto.WipDTO;
import com.agilesync.domain.enumeration.ScrumStagesEnum;
import org.springframework.stereotype.Service;

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

			if (isGradientDifference(sprints, i)) {
				patterns.computeIfAbsent("Diferença no Gradiente", k -> new ArrayList<>()).add(sprintNumber);
			}
			if (isFlatLines(sprint.getThroughput())) {
				patterns.computeIfAbsent("Linhas Planas", k -> new ArrayList<>()).add(sprintNumber);
			}
			if (isSCurve(sprints, i)) {
				patterns.computeIfAbsent("Curva-S", k -> new ArrayList<>()).add(sprintNumber);
			}
			if (isBulgingSpacing(sprints, i)) {
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
				report.append("O WIP total cresceu em relação à sprint anterior enquanto o throughput não aumentou (manteve-se ou caiu), ")
						.append("ou seja, a taxa de chegada de itens superou a taxa de saída, indicando divergência entre as inclinações das curvas de entrada e saída do CFD.");
				break;
			case "Linhas Planas":
				report.append("Total de itens prontos (throughput) igual a zero, indicando estagnação do fluxo de trabalho (nenhuma saída no período).");
				break;
			case "Curva-S":
				report.append("Uma sprint sem nenhuma entrega (throughput zero) foi seguida por uma sprint com entregas, ")
						.append("criando uma transição de um trecho plano para um trecho inclinado na curva cumulativa - o formato em \"S\" característico desse padrão.");
				break;
			case "Espaçamentos Protuberantes":
				report.append("O WIP total aumentou mais de 50% em relação à sprint anterior, indicando que a faixa de trabalho em progresso está se alargando, ")
						.append("ou seja, itens estão se acumulando mais rápido do que são concluídos.");
				break;
			case "Degraus da Escada":
				report.append("O throughput da sprint atual superou em mais de 50% o throughput da sprint anterior, ")
						.append("um salto súbito que sugere entregas concentradas em lote após um período de baixa entrega.\n");
				break;
			case "Espaçamentos Desaparecendo":
				report.append("Nenhum item passou pela fase 'Desenvolvimento' nesta sprint, mas houve itens concluídos em 'Pronto', ")
						.append("indicando que itens podem estar pulando essa etapa do fluxo (gap oculto).");
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

	private static int totalWip(SprintCfdDataDTO sprint) {
		return sprint.getWipsByStage().stream().mapToInt(WipDTO::getQuantity).sum();
	}

	private static boolean isGradientDifference(List<SprintCfdDataDTO> sprints, int currentIndex) {
		if (currentIndex == 0) {
			return false;
		}

		int currentWip = totalWip(sprints.get(currentIndex));
		int previousWip = totalWip(sprints.get(currentIndex - 1));
		int currentThroughput = sprints.get(currentIndex).getThroughput();
		int previousThroughput = sprints.get(currentIndex - 1).getThroughput();

		int wipTrend = currentWip - previousWip;
		int throughputTrend = currentThroughput - previousThroughput;

		return wipTrend > 0 && throughputTrend <= 0;
	}

	private static boolean isFlatLines(int throughput) {
		return throughput == 0;
	}

	private static boolean isSCurve(List<SprintCfdDataDTO> sprints, int currentIndex) {
		if (currentIndex == 0) {
			return false;
		}

		int previousThroughput = sprints.get(currentIndex - 1).getThroughput();
		int currentThroughput = sprints.get(currentIndex).getThroughput();

		return previousThroughput == 0 && currentThroughput > 0;
	}

	private static boolean isBulgingSpacing(List<SprintCfdDataDTO> sprints, int currentIndex) {
		if (currentIndex == 0) {
			return false;
		}

		int currentWip = totalWip(sprints.get(currentIndex));
		int previousWip = totalWip(sprints.get(currentIndex - 1));

		return previousWip > 0 && currentWip > previousWip * 1.5;
	}

	private static boolean isStairSteps(List<SprintCfdDataDTO> sprints, int currentIndex) {
		if (currentIndex == 0) {
			return false;
		}

		int previousThroughput = sprints.get(currentIndex - 1).getThroughput();
		int currentThroughput = sprints.get(currentIndex).getThroughput();

		return currentThroughput > 0 && currentThroughput > previousThroughput * 1.5;
	}

	private static boolean isDisappearingSpacing(SprintCfdDataDTO sprint) {
		boolean desenvolvimentoVazio = sprint.getCfdDatas().stream()
				.filter(data -> data.getStage() == ScrumStagesEnum.DESENVOLVIMENTO)
				.allMatch(data -> data.getQuantityCards() == 0);

		boolean prontoComItens = sprint.getCfdDatas().stream()
				.filter(data -> data.getStage() == ScrumStagesEnum.PRONTO)
				.anyMatch(data -> data.getQuantityCards() > 0);

		return desenvolvimentoVazio && prontoComItens;
	}
}
