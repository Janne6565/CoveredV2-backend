package com.janne.coveredv2.service;

import com.janne.coveredv2.dtos.steamgriddbapi.GridDto;
import com.janne.coveredv2.entities.Cover;
import com.janne.coveredv2.entities.Game;
import com.janne.coveredv2.repositories.CoverRepository;
import com.janne.coveredv2.repositories.GameRepository;
import com.janne.coveredv2.service.apis.SteamGridDBApiService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Slf4j
@Service
public class CoverService {

	private final SteamGridDBApiService steamGridDBApiService;
	private final CoverRepository coverRepository;
	private final GameRepository gameRepository;

	public CoverService(SteamGridDBApiService steamGridDBApiService, CoverRepository coverRepository, GameRepository gameRepository, MeterRegistry meterRegistry) {
		this.steamGridDBApiService = steamGridDBApiService;
		this.coverRepository = coverRepository;
		this.gameRepository = gameRepository;

		meterRegistry.gauge("app_cover_count", coverRepository, CoverRepository::count);
	}

	public List<Cover> getCoversFromGameId(String gameId) {
		return coverRepository.getCoversByGameUuid(gameId);
	}

	public Mono<List<Cover>> fetchCoversFromSteamId(long steamId) {
		return steamGridDBApiService.getSteamGridDbIdFromSteamAppId(steamId)
				.flatMap(steamGridDBApiService::getAllGridsFromGridDbId)
				.map(grids -> Arrays.stream(grids.toArray(GridDto[]::new))
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

	public Page<Cover> getAllCovers(Pageable pageable) {
		return coverRepository.findAll(pageable);
	}

	public List<Cover> getCoversFromGameIds(String[] gamesUuids) {
		return coverRepository.findByGameUuidIn(Arrays.asList(gamesUuids));
	}

	/**
	 * Builds a ZIP file (as bytes) structured as:
	 * {steam_id}/library_600x900.jpg
	 * For each provided cover UUID:
	 * - load Cover; if missing -> skip with debug
	 * - ensure cover.gameUuid present; load Game; ensure game.steamId present -> else skip with debug
	 * - fetch image bytes from cover.url (via SteamGridDBApiService); on 404/other error -> skip with debug
	 * - add to ZIP under "<steamId>/library_600x900.jpg" (always .jpg as requested)
	 */
	public byte[] buildCoversZip(List<String> coverUuids) {
		if (coverUuids == null || coverUuids.isEmpty()) {
			return emptyZip();
		}

		var executor = java.util.concurrent.Executors.newFixedThreadPool(
				Math.max(2, Runtime.getRuntime().availableProcessors()),
				r -> {
					Thread t = new Thread(r, "covers-zip-worker");
					t.setDaemon(true);
					return t;
				}
		);

		try {
			List<CompletableFuture<CoverZipItem>> futures = coverUuids.stream()
					.map(uuid -> CompletableFuture.supplyAsync(() -> toZipItem(uuid), executor))
					.toList();

			List<CoverZipItem> items = futures.stream()
					.map(f -> {
						try {
							return f.get();
						} catch (Exception e) {
							log.debug("Skipping cover due to async error", e);
							return null;
						}
					})
					.filter(i -> i != null && i.imageBytes != null && i.imageBytes.length > 0)
					.toList();

			try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
			     ZipOutputStream zos = new ZipOutputStream(baos)) {
				for (CoverZipItem item : items) {
					String path = item.steamId + "/" + item.capsuleFilename;
					ZipEntry entry = new ZipEntry(path);
					zos.putNextEntry(entry);
					zos.write(item.imageBytes);
					zos.closeEntry();
				}
				zos.finish();
				return baos.toByteArray();
			} catch (IOException e) {
				log.debug("Failed to build ZIP", e);
				return emptyZip();
			}
		} finally {
			executor.shutdown();
		}
	}

	private CoverZipItem toZipItem(String coverUuid) {
		Optional<Cover> coverOpt = coverRepository.findById(coverUuid);
		if (coverOpt.isEmpty()) {
			log.debug("Cover uuid not found, skipping: {}", coverUuid);
			return null;
		}
		Cover cover = coverOpt.get();

		if (cover.getGameUuid() == null || cover.getGameUuid().isBlank()) {
			log.debug("Cover {} has no gameUuid, skipping", coverUuid);
			return null;
		}

		Optional<Game> gameOpt = gameRepository.findById(cover.getGameUuid());
		if (gameOpt.isEmpty()) {
			log.debug("Game not found for cover {}, gameUuid={}, skipping", coverUuid, cover.getGameUuid());
			return null;
		}
		Game game = gameOpt.get();

		if (game.getSteamId() == null) {
			log.debug("Game {} has no steamId, skipping cover {}", game.getUuid(), coverUuid);
			return null;
		}

		byte[] imageBytes = steamGridDBApiService.downloadImageBytes(cover.getUrl()).block();
		if (imageBytes == null || imageBytes.length == 0) {
			// Already logged inside the API service
			return null;
		}

		CoverZipItem item = new CoverZipItem();
		item.steamId = game.getSteamId();
		item.imageBytes = imageBytes;
		item.capsuleFilename = game.getCapsuleFilename();
		return item;
	}

	private byte[] emptyZip() {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
		     ZipOutputStream zos = new ZipOutputStream(baos)) {
			zos.finish();
			return baos.toByteArray();
		} catch (IOException e) {
			return new byte[0];
		}
	}

	private static class CoverZipItem {
		Long steamId;
		byte[] imageBytes;
		String capsuleFilename;
	}
}
