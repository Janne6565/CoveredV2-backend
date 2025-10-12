package com.janne.coveredv2.controller;

import com.janne.coveredv2.entities.Cover;
import com.janne.coveredv2.service.CoverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(BaseController.BASE_URL + "/covers")
public class CoverController {

	private final CoverService coverService;

	@GetMapping
	public ResponseEntity<List<Cover>> getAllCovers() {
		return ResponseEntity.ok(coverService.getAllCovers());
	}

	@GetMapping("/game/{gameId}")
	public ResponseEntity<List<Cover>> getCoversFromGameId(@PathVariable String gameId) {
		return ResponseEntity.ok(coverService.getCoversFromGameId(gameId));
	}
}
