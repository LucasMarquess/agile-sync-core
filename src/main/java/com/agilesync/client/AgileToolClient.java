package com.agilesync.client;

import com.agilesync.domain.dto.BoardDTO;
import com.agilesync.domain.dto.CardDTO;
import com.agilesync.domain.dto.LabelDTO;
import com.agilesync.domain.dto.ListDTO;

import java.util.List;

public interface AgileToolClient {

	List<? extends BoardDTO> getBoards(String apiKey, String token, String fields);

	List<? extends ListDTO> getBoardLists(String boardId, String apiKey, String token, String fields);

	List<? extends LabelDTO> getBoardLabels(String boardId, String apiKey, String token, String fields);

	List<? extends CardDTO> getCardsFromList(String listId, String apiKey, String token, String fields);
}
