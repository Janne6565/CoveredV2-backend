package com.janne.coveredv2.service;

import com.janne.coveredv2.dtos.steamapi.UserGameLibraryDto;
import com.janne.coveredv2.entities.Cover;
import com.janne.coveredv2.entities.Game;
import com.janne.coveredv2.repositories.GameRepository;
import com.janne.coveredv2.service.apis.SteamApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

	private final GameRepository gameRepository;
	private final SteamApiService steamApiService;
	private final CoverService coverService;

	@Scheduled(fixedDelay = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
	private void fetchUnfetchedGames() {
		List<Game> unfetchedGames = gameRepository.getGamesWithoutFetchedCovers();
		if (unfetchedGames.isEmpty()) {
			return;
		}
		log.info("Found {} games without fetched covers", unfetchedGames.size());

		unfetchedGames
				.forEach(game -> {
							log.info("Fetching covers for game {}", game.getName());
							coverService.fetchCoversFromSteamId(game.getSteamId())
									.subscribe(
											covers -> {
												Game reloadedGame = gameRepository.findBySteamId(game.getSteamId()).orElseThrow();
												reloadedGame.setTimeOfLastCoverFetch(System.currentTimeMillis());
												covers.forEach(cover -> cover.setGameUuid(reloadedGame.getUuid()));
												coverService.saveCovers(covers.toArray(Cover[]::new));
												gameRepository.save(reloadedGame);
												log.info("Fetched covers for game {}", reloadedGame.getName());
											}
									);
						}
				);
	}

	public Game[] getAllGames() {
		return gameRepository.findAll().toArray(new Game[0]);
	}

	public Game saveGame(Game game) {
		return gameRepository.save(game);
	}

	public Game[] getGamesFromPlayer(Long steamUserId) {
		UserGameLibraryDto userGameLibraryDto = steamApiService.getUserGameLibrary(steamUserId).block();

		if (userGameLibraryDto == null) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Steam API response was null");
		}
		List<UserGameLibraryDto.Game> games = userGameLibraryDto.getResponse().getGames();

		return Flux.fromIterable(games)
				.flatMap(game -> getGameFromSteamId(game.getAppid()))
				.collectList()
				.map(list -> list.toArray(new Game[0]))
				.map(list -> {
					log.info("Loaded {} Games from User with SteamId {}", list.length, steamUserId);
					return list;
				})
				.block();
	}

	private Mono<Game> getGameFromSteamId(Long appid) {
		if (gameRepository.findBySteamId(appid).isPresent()) {
			return Mono.just(gameRepository.findBySteamId(appid).get());
		}

		return steamApiService.fetchGameFromSteamId(appid)
				.map(this::saveGame);
	}
}
