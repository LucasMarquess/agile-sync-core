package com.agilesync.client;

import com.agilesync.domain.dto.TrelloBoardDTO;
import com.agilesync.domain.dto.TrelloListDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

	@GetMapping("/boards/{boardId}/lists")
	List<TrelloListDTO> getBoardLists(
			@PathVariable("boardId") String boardId,
			@RequestParam("key") String apiKey,
			@RequestParam("token") String token,
			@RequestParam("fields") String fields
	);
}
