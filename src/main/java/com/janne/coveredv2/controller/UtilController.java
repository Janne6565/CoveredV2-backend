package com.janne.coveredv2.controller;

import com.janne.coveredv2.dtos.ProfileValidityResponse;
import com.janne.coveredv2.dtos.SteamIdResponse;
import com.janne.coveredv2.service.apis.SteamApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(BaseController.BASE_URL + "/util")
public class UtilController {

	private final SteamApiService steamApiService;

	@GetMapping("/steam/resolve-vanity-url/{vanityUrl}")
	public ResponseEntity<SteamIdResponse> getUserGameLibrary(@PathVariable String vanityUrl) {
		return ResponseEntity.ok(SteamIdResponse.builder()
				.steamid(steamApiService.resolveSteamIdFromSteamVanityUrl(vanityUrl))
				.build());
	}

	@GetMapping("/steam/username/{steamId}")
	public ResponseEntity<String> getUserName(@PathVariable Long steamId) {
		return ResponseEntity.ok(steamApiService.getSteamUserName(steamId));
	}

    @GetMapping("/steam/profile-validity/{steamId}")
    public ResponseEntity<ProfileValidityResponse> getProfileValidity(@PathVariable Long steamId) {
        return ResponseEntity.ok(steamApiService.isProfileValid(steamId));
    }
}
