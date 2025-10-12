package com.janne.coveredv2.controller;

import com.janne.coveredv2.entities.Game;
import com.janne.coveredv2.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(BaseController.BASE_URL + "/games")
public class GameController {

	private final GameService gameService;

	@GetMapping
	public ResponseEntity<Game[]> getAllGames() {
		return ResponseEntity.ok(gameService.getAllGames());
	}

	@GetMapping("/player/{playerId}")
	public ResponseEntity<Game[]> getGamesFromPlayer(@PathVariable Long playerId) {
		return ResponseEntity.ok(gameService.getGamesFromPlayer(playerId));
	}

}
