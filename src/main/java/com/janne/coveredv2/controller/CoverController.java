package com.janne.coveredv2.controller;

import com.janne.coveredv2.entities.Cover;
import com.janne.coveredv2.service.CoverService;
import com.janne.coveredv2.service.MetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(BaseController.BASE_URL + "/covers")
public class CoverController {

	private final CoverService coverService;
	private final MetricService metricService;

	@GetMapping
	public ResponseEntity<Page<Cover>> getAllCovers(@PageableDefault(size = 50) Pageable pageable) {
		metricService.incrementCounter("app_all_covers_requests");
		return ResponseEntity.ok(coverService.getAllCovers(pageable));
	}

	@GetMapping("/game/{gameId}")
	public ResponseEntity<List<Cover>> getCoversFromGameId(@PathVariable String gameId) {
		metricService.incrementCounter("app_cover_from_game_request", List.of("gameId", gameId));
		return ResponseEntity.ok(coverService.getCoversFromGameId(gameId));
	}

	@PostMapping("/games")
	public ResponseEntity<List<Cover>> getCoversFromGameIds(@RequestBody String[] gamesUuids) {
		metricService.incrementCounter("app_cover_from_games_request", List.of("game_count", String.valueOf(gamesUuids.length)));
		return ResponseEntity.ok(coverService.getCoversFromGameIds(gamesUuids));
	}

	@PostMapping(value = "/export", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<byte[]> exportCoversZip(@RequestBody java.util.List<String> coverUuids) {
		metricService.incrementCounter("app_export_covers_request", List.of("cover_count", String.valueOf(coverUuids.size())));
		byte[] zipBytes = coverService.buildCoversZip(coverUuids);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"export.zip\"")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(zipBytes);
	}

}
