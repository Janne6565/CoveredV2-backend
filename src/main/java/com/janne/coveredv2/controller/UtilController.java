package com.janne.coveredv2.controller;

import com.janne.coveredv2.dtos.ProfileValidityResponse;
import com.janne.coveredv2.dtos.SteamIdResponse;
import com.janne.coveredv2.service.MetricService;
import com.janne.coveredv2.service.apis.SteamApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(BaseController.BASE_URL + "/util")
public class UtilController {

	private final SteamApiService steamApiService;
	private final MetricService metricService;

	@GetMapping("/steam/resolve-vanity-url/{vanityUrl}")
	public ResponseEntity<SteamIdResponse> getUserGameLibrary(@PathVariable String vanityUrl) {
		String steamId = steamApiService.resolveSteamIdFromSteamVanityUrl(vanityUrl);
		metricService.incrementCounter("app_resolve_vanity_url", List.of("vanity_url", vanityUrl, "steam_id", String.valueOf(steamId)));
		return ResponseEntity.ok(SteamIdResponse.builder()
				.steamid(steamId)
				.build());
	}

	@GetMapping("/steam/username/{steamId}")
	public ResponseEntity<String> getUserName(@PathVariable Long steamId) {
		String username = steamApiService.getSteamUserName(steamId);
		metricService.incrementCounter("app_username_requests", List.of("username", username, "steam_id", String.valueOf(steamId)));
		return ResponseEntity.ok(username);
	}

	@GetMapping("/steam/profile-validity/{steamId}")
	public ResponseEntity<ProfileValidityResponse> getProfileValidity(@PathVariable Long steamId) {
		ProfileValidityResponse profileValidityResponse = steamApiService.isProfileValid(steamId);
		metricService.incrementCounter("app_profile_validity_requests",
				List.of(
						"steam_id", String.valueOf(steamId),
						"valid", String.valueOf(profileValidityResponse.isValid()),
						"reason", String.valueOf(profileValidityResponse.getReason())
				));
		return ResponseEntity.ok(profileValidityResponse);
	}
}
