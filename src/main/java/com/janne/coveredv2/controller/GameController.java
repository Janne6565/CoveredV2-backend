package com.janne.coveredv2.controller;

import com.janne.coveredv2.dtos.GameWithPlaytime;
import com.janne.coveredv2.entities.Game;
import com.janne.coveredv2.service.GameService;
import com.janne.coveredv2.service.MetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(BaseController.BASE_URL + "/games")
public class GameController {

	private final GameService gameService;
	private final MetricService metricService;

	@GetMapping
	public ResponseEntity<Page<Game>> getAllGames(@PageableDefault(size = 50) Pageable pageable,
	                                              @RequestParam(value = "search", required = false) String search) {
		metricService.incrementCounter("app_all_games_requests");
		return ResponseEntity.ok(gameService.getAllGames(pageable, search));
	}

	@GetMapping("/player/{playerId}")
	public ResponseEntity<GameWithPlaytime[]> getGamesFromPlayer(@PathVariable Long playerId) {
		GameWithPlaytime[] games = gameService.getGamesFromPlayer(playerId);
		metricService.incrementCounter("app_get_games_from_player", List.of(
				"steam_id", String.valueOf(playerId),
				"game_count", String.valueOf(games.length),
				"unresolved_games", String.valueOf(
						Arrays.stream(games).filter(game ->
								game.getGame().getTimeOfLastCoverFetch() == null
						).toList().size())
		));
		return ResponseEntity.ok(games);
	}

	@GetMapping("/family/{userId}")
	public ResponseEntity<GameWithPlaytime[]> getGamesFromSteamFamilyLibrary(@PathVariable Long userId,
	                                                                         @RequestParam("token") String userApiToken) {
		GameWithPlaytime[] games = gameService.getGameFromSteamFamilyLibrary(userId, userApiToken);
		metricService.incrementCounter("app_get_games_from_player_family", List.of(
				"steam_id", String.valueOf(userId),
				"game_count", String.valueOf(games.length),
				"unresolved_games", String.valueOf(
						Arrays.stream(games).filter(game ->
								game.getGame().getTimeOfLastCoverFetch() == null
						).toList().size())
		));
		return ResponseEntity.ok(games);
	}
}
