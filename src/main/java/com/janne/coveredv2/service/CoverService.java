package com.janne.coveredv2.service;

import com.janne.coveredv2.entities.Cover;
import com.janne.coveredv2.repositories.CoverRepository;
import com.janne.coveredv2.repositories.GameRepository;
import com.janne.coveredv2.service.apis.SteamGridDBApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoverService {

	private final SteamGridDBApiService steamGridDBApiService;
	private final CoverRepository coverRepository;
	private final GameRepository gameRepository;

	public List<Cover> getCoversFromGameId(String gameId) {
		return coverRepository.getCoversByGameUuid(gameId);
	}

	public Mono<List<Cover>> fetchCoversFromSteamId(long steamId) {
		return steamGridDBApiService.getSteamGridDbIdFromSteamAppId(steamId)
				.flatMap(steamGridDBApiService::getGridsFromGridDbId)
				.map(grids -> Arrays.stream(grids)
						.map(steamGridDBApiService::convertSteamGridDBCover)
						.toList())
				.onErrorResume(ResponseStatusException.class, ex -> {
					if (ex.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
						return Mono.fromRunnable(() ->
										gameRepository.findBySteamId(steamId).ifPresent(game -> {
											log.info("SteamGridDB cover not found for game {}, setting steamGridDbMissing to true", game.getName());
											game.setSteamGridDbMissing(true);
											gameRepository.save(game);
										}))
								.subscribeOn(Schedulers.boundedElastic())
								.thenReturn(List.of());
					}
					return Mono.error(ex);
				});
	}

	public List<Cover> saveCovers(Cover[] covers) {
		return coverRepository.saveAll(Arrays.asList(covers));
	}

	public List<Cover> getAllCovers() {
		return coverRepository.findAll();
	}
}
