package com.agilesync.service;

import com.agilesync.domain.dto.CfdDataDTO;
import com.agilesync.domain.dto.WipDTO;
import com.agilesync.domain.enumeration.ScrumTrelloEnum;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportsService {
	private final ApplicationContext applicationContext;
	private final TrelloIntegrationService trelloIntegrationService;

	public byte[] generateReportTrello(String initialPeriod, String finalPeriod) throws JRException, IOException {
		var metric = this.trelloIntegrationService.generateMetrics(initialPeriod, finalPeriod, true);
		List<CfdDataDTO> cfdDatas = new ArrayList<>();
		List<ScrumTrelloEnum> stageOrder = List.of(
				ScrumTrelloEnum.PRONTO,
				ScrumTrelloEnum.TESTES,
				ScrumTrelloEnum.DESENVOLVIMENTO,
				ScrumTrelloEnum.BACKLOG
		);

		var sprintsWips = new ArrayList<WipDTO>();
		metric.getSprintCfdData().forEach(x -> {
			cfdDatas.addAll(x.getCfdDatas());
			x.getWipsByStage().forEach(wipDTO -> {
				wipDTO.setSprintNumber(x.getSprintNumber());
				sprintsWips.add(wipDTO);
			});
		});
		cfdDatas.sort(Comparator.comparingInt(dto -> stageOrder.indexOf(dto.getStage())));

		String pathRelatorios = applicationContext.getResource("classpath:/reports").getFile().getPath();

		HashMap hashMap = new HashMap();
		hashMap.put("INITIAL_SPRINT", initialPeriod);
		hashMap.put("FINAL_SPRINT", finalPeriod);
		hashMap.put("VELOCITY", metric.getVelocity());
		hashMap.put("ANALYSIS", metric.getAnalysis());
		hashMap.put("CFD_DATAS", cfdDatas);
		hashMap.put("SPRINTS_METRICS", metric.getSprintCfdData());
		hashMap.put("WIPS", sprintsWips);
		hashMap.put("REPORT_LOCALE", new Locale("pt", "BR"));

		String jasperFilePath = pathRelatorios + "/report-metrics.jasper";
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperFilePath, hashMap, new JREmptyDataSource());
		return JasperExportManager.exportReportToPdf(jasperPrint);
	}
}
