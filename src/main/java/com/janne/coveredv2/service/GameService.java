package com.janne.coveredv2.service;

import com.janne.coveredv2.dtos.GameWithPlaytime;
import com.janne.coveredv2.dtos.steamapi.SharedLibraryAppsDto;
import com.janne.coveredv2.dtos.steamapi.UserGameLibraryDto;
import com.janne.coveredv2.entities.Cover;
import com.janne.coveredv2.entities.Game;
import com.janne.coveredv2.repositories.GameRepository;
import com.janne.coveredv2.service.apis.SteamApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

	private final GameRepository gameRepository;
	private final SteamApiService steamApiService;
	private final CoverService coverService;
	@Value("${app.steamgriddb.concurrency_count:50}")
	private int concurrencyCount;

	@Scheduled(fixedDelay = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
	private void fetchUnfetchedGames() {
		List<Game> unfetchedGames = gameRepository.getGamesWithoutFetchedCovers();
		if (!unfetchedGames.isEmpty()) {
			log.info("Found {} games without fetched covers", unfetchedGames.size());

			int concurrency = Math.min(concurrencyCount, unfetchedGames.size());

			Flux.fromIterable(unfetchedGames)
					.flatMap(game ->
									coverService.fetchCoversFromSteamId(game.getSteamId())
											.doOnSubscribe(s -> log.info("Fetching covers for game {}", game.getName()))
											.map(covers -> Tuples.of(game, covers, true))
											.onErrorResume(ex -> Mono.just(Tuples.of(game, List.of(), false))),
							concurrency
					)
					.doOnNext((Tuple3<Game, List<Cover>, Boolean> tuple) -> {
						Game game = tuple.getT1();
						List<Cover> covers = tuple.getT2();
						if (!tuple.getT3()) {
							log.warn("Failed to fetch covers for game {}", game.getName());
							return;
						}

						Game reloadedGame = gameRepository.findBySteamId(game.getSteamId()).orElseThrow();
						reloadedGame.setTimeOfLastCoverFetch(System.currentTimeMillis());
						covers.forEach(cover -> cover.setGameUuid(reloadedGame.getUuid()));
						if (!covers.isEmpty()) {
							coverService.saveCovers(covers.toArray(Cover[]::new));
						}
						gameRepository.save(reloadedGame);
						log.info("Fetched {} covers for game {}", covers.size(), reloadedGame.getName());
					})
					.then()
					.block(); // Block until all parallel fetches complete
		}
	}

	public Game[] getAllGames() {
		return gameRepository.findAll().toArray(new Game[0]);
	}

	public Page<Game> getAllGames(Pageable pageable) {
		return gameRepository.findAll(pageable);
	}

	public Page<Game> getAllGames(Pageable pageable, String search) {
		if (search == null || search.isBlank()) {
			return gameRepository.findAll(pageable);
		}
		return gameRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
	}

	public Game saveGame(Game game) {
		return gameRepository.save(game);
	}

	public GameWithPlaytime[] getGamesFromPlayer(Long steamUserId) {
		UserGameLibraryDto userGameLibraryDto = steamApiService.getUserGameLibrary(steamUserId).block();

		if (userGameLibraryDto == null) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Steam API response was null");
		}
		List<UserGameLibraryDto.Game> games = userGameLibraryDto.getResponse().getGames();

		return games.stream()
				.map(game -> GameWithPlaytime.builder()
						.game(getGameFromSteamId(game.getAppid(), game.getName(), game.getCapsuleFilename()))
						.playtime(game.getPlaytimeForever())
						.build()
				)
				.toArray(GameWithPlaytime[]::new);
	}

	public GameWithPlaytime[] getGameFromSteamFamilyLibrary(Long steamUserId, String userApiToken) {
		try {
			Long familyId = steamApiService.getSteamFamilyIdForUser(steamUserId, userApiToken).block();
			if (familyId == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No Steam family group found for user");
			}

			List<SharedLibraryAppsDto.App> appIds = steamApiService.getUserFamilyGameIds(familyId, userApiToken);
			if (appIds == null || appIds.isEmpty()) {
				log.info("No shared library apps found for familyId {}", familyId);
				return new GameWithPlaytime[0];
			}

			return appIds.stream()
					.map(app -> GameWithPlaytime.builder()
							.game(getGameFromSteamId(app.getAppid(), app.getName(), app.getCapsuleFilename()))
							.playtime(app.getRtPlaytime())
							.build()
					)
					.toArray(GameWithPlaytime[]::new);
		} catch (Exception e) {
			return getGamesFromPlayer(steamUserId);
		}
	}

	private Game getGameFromSteamId(Long appid, String gameName, String capsuleFilename) {
		Optional<Game> existing = gameRepository.findBySteamId(appid);
		if (existing.isPresent()) {
			return existing.get();
		}
		Game game = steamApiService.buildGameFromSteamGameId(appid, gameName, capsuleFilename);

		return gameRepository.save(game);
	}

	public Game loadGame(Long steamGameId, String gameName, String capsuleFilename) {
		Game game = getGameFromSteamId(steamGameId, gameName, capsuleFilename);
		return gameRepository.save(game);
	}
}
