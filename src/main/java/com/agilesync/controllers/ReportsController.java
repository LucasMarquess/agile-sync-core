package com.agilesync.controllers;

import com.agilesync.service.ReportsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("reports")
@RequiredArgsConstructor
@Slf4j
public class ReportsController {

	private final ReportsService reportsService;

	@GetMapping("metrics-trello")
	public ResponseEntity gerarRelatorioPdf(@RequestParam String initialPeriod, @RequestParam String finalPeriod) {
		try {
			byte[] relatorio = reportsService.generateReportTrello(initialPeriod, finalPeriod);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType("application/pdf"));
			headers.setContentDispositionFormData("relatorio-metricas.pdf", "relatorio-metricas.pdf");
			headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
			return new ResponseEntity(relatorio, headers, HttpStatus.OK);
		} catch (Exception ex) {
			log.error("Falha na geração do relatorio de métricas", ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}
