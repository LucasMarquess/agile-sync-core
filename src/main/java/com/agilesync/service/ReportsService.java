package com.agilesync.service;

import com.agilesync.domain.dto.CfdDataDTO;
import com.agilesync.domain.dto.WipDTO;
import com.agilesync.domain.enumeration.ScrumStagesEnum;
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

	public byte[] generateReportTrello(Long integrationId, String initialPeriod, String finalPeriod) throws JRException, IOException {
		var integration = this.trelloIntegrationService.getById(integrationId);
		var metric = this.trelloIntegrationService.generateMetrics(integrationId, initialPeriod, finalPeriod, true);
		List<CfdDataDTO> cfdDatas = new ArrayList<>();
		List<ScrumStagesEnum> stageOrder = List.of(
				ScrumStagesEnum.PRONTO,
				ScrumStagesEnum.TESTES,
				ScrumStagesEnum.DESENVOLVIMENTO,
				ScrumStagesEnum.BACKLOG
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
		hashMap.put("INTEGRATION_NAME", integration.getName());
		hashMap.put("REPORT_LOCALE", new Locale("pt", "BR"));

		String jrxmlFilePath = pathRelatorios + "/report-metrics.jrxml";
		JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlFilePath);
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, hashMap, new JREmptyDataSource());
		return JasperExportManager.exportReportToPdf(jasperPrint);
	}
}
