package com.janne.coveredv2.controller;

import com.janne.coveredv2.entities.Game;
import com.janne.coveredv2.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequiredArgsConstructor
@RequestMapping(BaseController.BASE_URL + "/games")
public class GameController {

	private final GameService gameService;

	@GetMapping
	public ResponseEntity<Page<Game>> getAllGames(@PageableDefault(size = 50) Pageable pageable,
	                                              @RequestParam(value = "search", required = false) String search) {
		return ResponseEntity.ok(gameService.getAllGames(pageable, search));
	}

	@GetMapping("/player/{playerId}")
	public ResponseEntity<Game[]> getGamesFromPlayer(@PathVariable Long playerId) {
		return ResponseEntity.ok(gameService.getGamesFromPlayer(playerId));
	}

	@GetMapping("/family/{userId}")
	public ResponseEntity<Game[]> getGamesFromSteamFamilyLibrary(@PathVariable Long userId,
	                                                             @RequestParam("token") String userApiToken) {
		return ResponseEntity.ok(gameService.getGameFromSteamFamilyLibrary(userId, userApiToken));
	}

	@PostMapping("/steam/{steamGameId}")
	public ResponseEntity<Game> loadGameFromSteam(@PathVariable Long steamGameId, @RequestBody Game game) {
		return ResponseEntity.ok(gameService.loadGame(steamGameId, game.getName(), game.getCapsuleFilename()));
	}
}
