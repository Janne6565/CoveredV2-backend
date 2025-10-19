package com.janne.coveredv2.controller;

import com.janne.coveredv2.service.apis.SteamApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(BaseController.BASE_URL + "/util")
public class UtilController {

	private final SteamApiService steamApiService;

	@RequestMapping("/steam/resolve-vanity-url/{vanityUrl}")
	public ResponseEntity<String> getUserGameLibrary(@PathVariable String vanityUrl) {
		return ResponseEntity.ok(steamApiService.resolveSteamIdFromSteamVanityUrl(vanityUrl));
	}
}
