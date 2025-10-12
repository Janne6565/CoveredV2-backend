package com.janne.coveredv2.service;

import com.janne.coveredv2.dtos.steamapi.SharedLibraryAppsDto;
import com.janne.coveredv2.dtos.steamapi.UserGameLibraryDto;
import com.janne.coveredv2.entities.Cover;
import com.janne.coveredv2.entities.Game;
import com.janne.coveredv2.repositories.GameRepository;
import com.janne.coveredv2.service.apis.SteamApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

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
		if (!unfetchedGames.isEmpty()) {
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
													log.info("Fetched {} covers for game {}", covers.size(), reloadedGame.getName());
												}
										);
							}
					);
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

	public Game[] getGamesFromPlayer(Long steamUserId) {
		UserGameLibraryDto userGameLibraryDto = steamApiService.getUserGameLibrary(steamUserId).block();

		if (userGameLibraryDto == null) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Steam API response was null");
		}
		List<UserGameLibraryDto.Game> games = userGameLibraryDto.getResponse().getGames();

		return games.stream()
				.map(game -> getGameFromSteamId(game.getAppid(), game.getName()))
				.toArray(Game[]::new);
	}

	public Game[] getGameFromSteamFamilyLibrary(Long steamUserId, String userApiToken) {
		Long familyId = steamApiService.getSteamFamilyIdForUser(steamUserId, userApiToken).block();
		if (familyId == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No Steam family group found for user");
		}

		List<SharedLibraryAppsDto.App> appIds = steamApiService.getUserFamilyGameIds(familyId, userApiToken);
		if (appIds == null || appIds.isEmpty()) {
			log.info("No shared library apps found for familyId {}", familyId);
			return new Game[0];
		}

		return appIds.stream()
				.map(app -> getGameFromSteamId(app.getAppid(), app.getName()))
				.toArray(Game[]::new);
	}

	private Game getGameFromSteamId(Long appid, String gameName) {
		Optional<Game> existing = gameRepository.findBySteamId(appid);
		if (existing.isPresent()) {
			return existing.get();
		}
		Game game = steamApiService.buildGameFromSteamGameId(appid, gameName);

		return gameRepository.save(game);
	}
}
