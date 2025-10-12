package com.janne.coveredv2.controller;

import com.janne.coveredv2.entities.Cover;
import com.janne.coveredv2.service.CoverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequiredArgsConstructor
@RequestMapping(BaseController.BASE_URL + "/covers")
public class CoverController {

	private final CoverService coverService;

	@GetMapping
	public ResponseEntity<Page<Cover>> getAllCovers(@PageableDefault(size = 50) Pageable pageable) {
		return ResponseEntity.ok(coverService.getAllCovers(pageable));
	}

	@GetMapping("/game/{gameId}")
	public ResponseEntity<List<Cover>> getCoversFromGameId(@PathVariable String gameId) {
		return ResponseEntity.ok(coverService.getCoversFromGameId(gameId));
	}

}
