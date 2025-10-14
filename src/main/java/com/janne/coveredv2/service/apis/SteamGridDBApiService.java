package com.janne.coveredv2.service.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.janne.coveredv2.dtos.steamgriddbapi.GameDetailDto;
import com.janne.coveredv2.dtos.steamgriddbapi.GridDto;
import com.janne.coveredv2.dtos.steamgriddbapi.GridsDetailDto;
import com.janne.coveredv2.entities.Cover;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SteamGridDBApiService {

	private final WebClient webClient = WebClient.create();
	private final ObjectMapper objectMapper;
	@Value("${app.steamgriddb.api_key}")
	private String API_KEY;

	public Mono<Long> getSteamGridDbIdFromSteamAppId(long steamAppId) {
		return Objects.requireNonNull(webClient.get()
				.uri("https://www.steamgriddb.com/api/v2/games/steam/" + steamAppId)
				.header("Authorization", "Bearer " + API_KEY)
				.retrieve()
				.onStatus(status -> status.isSameCodeAs(HttpStatus.NOT_FOUND),
						clientResponse -> clientResponse.createException()
								.flatMap(e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
										"SteamGridDB game not found for steamAppId " + steamAppId))))
				.onStatus(status -> !status.is2xxSuccessful(),
						clientResponse -> clientResponse.createException().flatMap(Mono::error))
				.bodyToMono(GameDetailDto.class)
				.map(GameDetailDto::getData)
				.map(GameDetailDto.GameDetailObjectData::getId)
		);
	}

	public Mono<GridsDetailDto> getGridsPage(long gridDbId, int page) {
		return Objects.requireNonNull(webClient.get()
				.uri("https://www.steamgriddb.com/api/v2/grids/game/" + gridDbId +
						"?page=" + page + "&types=static,animated")
				.header("Authorization", "Bearer " + API_KEY)
				.retrieve()
				.onStatus(status -> status.isSameCodeAs(HttpStatus.NOT_FOUND),
						clientResponse -> clientResponse.createException()
								.flatMap(e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
										"No grids found for game with id " + gridDbId))))
				.onStatus(status -> !status.is2xxSuccessful(),
						clientResponse -> clientResponse.createException().flatMap(Mono::error))
				.bodyToMono(GridsDetailDto.class)
		);
	}

	public Mono<GridDto[]> getGridsFromGridDbId(long gridDbId) {
		return getGridsPage(gridDbId, 0)
				.map(GridsDetailDto::getData);
	}

	public Mono<List<GridDto>> getAllGridsFromGridDbId(long gridDbId) {
		return getGridsPage(gridDbId, 0).flatMap(firstPage -> {
			int total = firstPage.getTotal();
			int limit = firstPage.getLimit();
			if (limit <= 0) {
				return Mono.just(Arrays.asList(firstPage.getData()));
			}
			int pages = (int) Math.ceil((double) total / (double) limit); // total number of pages (0..pages-1)
			if (pages <= 1) {
				return Mono.just(Arrays.asList(firstPage.getData()));
			}
			Flux<GridsDetailDto> remaining = Flux.range(1, pages - 1)
					.concatMap(p -> getGridsPage(gridDbId, p));
			return Flux.concat(Flux.just(firstPage), remaining)
					.map(GridsDetailDto::getData)
					.flatMapIterable(Arrays::asList)
					.collectList();
		});
	}

	/**
	 * Downloads image bytes from a public URL using WebClient.
	 * - On 404 or any non-2xx status: returns null and logs at debug level
	 * - On error/timeout: returns null and logs at debug level
	 */
	public Mono<byte[]> downloadImageBytes(String url) {
		if (url == null || url.isBlank()) {
			log.debug("downloadImageBytes called with empty URL");
			return null;
		}
		try {
			return webClient.get()
					.uri(url)
					.exchangeToMono(response -> {
						HttpStatusCode code = response.statusCode();
						if (code.is2xxSuccessful()) {
							return response.bodyToFlux(org.springframework.core.io.buffer.DataBuffer.class)
									.timeout(Duration.ofSeconds(20))
									.as(DataBufferUtils::join)
									.map(db -> {
										byte[] bytes = new byte[db.readableByteCount()];
										db.read(bytes);
										DataBufferUtils.release(db);
										return bytes;
									});
						} else {
							if (code.value() == 404) {
								log.debug("Image URL returned 404, skipping: {}", url);
							} else {
								log.debug("Image URL returned {}, skipping: {}", code.value(), url);
							}
							return reactor.core.publisher.Mono.empty();
						}
					});
		} catch (Exception e) {
			log.debug("Failed to fetch image from {}, skipping. Error: {}", url, e.toString());
			return null;
		}
	}

	public Cover convertSteamGridDBCover(GridDto mono) {
		return Cover.builder()
				.url(mono.getUrl())
				.steamGridDbId(mono.getId())
				.style(mono.getStyle())
				.width(mono.getWidth())
				.height(mono.getHeight())
				.nsfw(mono.isNsfw())
				.humor(mono.isHumor())
				.notes(mono.getNotes())
				.mime(mono.getMime())
				.language(mono.getLanguage())
				.thumb(mono.getThumb())
				.build();
	}
}
