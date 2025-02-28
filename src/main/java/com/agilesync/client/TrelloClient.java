package com.agilesync.client;

import com.agilesync.domain.dto.TrelloBoardDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "trelloClient", url = "${api.trello.url}")
public interface TrelloClient {

	@GetMapping("/members/me/boards")
	List<TrelloBoardDTO> getBoards(
			@RequestParam("key") String apiKey,
			@RequestParam("token") String token,
			@RequestParam("fields") String fields
	);
}
