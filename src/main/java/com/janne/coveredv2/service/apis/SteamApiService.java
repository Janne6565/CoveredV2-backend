package com.janne.coveredv2.service.apis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.janne.coveredv2.dtos.steamapi.SharedLibraryAppsDto;
import com.janne.coveredv2.dtos.steamapi.SteamAppListResponse;
import com.janne.coveredv2.dtos.steamapi.UserGameLibraryDto;
import com.janne.coveredv2.entities.Game;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SteamApiService {

	private final WebClient webClient = WebClient.builder()
			.exchangeStrategies(ExchangeStrategies.builder()
					.codecs(configurer ->
							configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) // 16 MB
					)
					.build())
			.build();
	private final ObjectMapper objectMapper;
	@Value("${app.steam.api_key}")
	private String API_KEY;
	private Map<Integer, String> steamAppIdsToNames = new HashMap<>();

	@Scheduled(fixedDelay = 1, timeUnit = java.util.concurrent.TimeUnit.HOURS)
	private void reloadSteamAppIdsToNames() {
		steamAppIdsToNames = webClient.get()
				.uri("https://api.steampowered.com/ISteamApps/GetAppList/v2/")
				.retrieve()
				.bodyToMono(SteamAppListResponse.class)
				.map(response -> {
					Map<Integer, String> appMap = new HashMap<>();
					if (response != null && response.applist != null && response.applist.apps != null) {
						for (SteamAppListResponse.SteamApp app : response.applist.apps) {
							if (app.name != null && !app.name.isBlank()) {
								appMap.put(app.appid, app.name);
							}
						}
					}
					return appMap;
				}).block();
		log.info("Loaded {} Steam app names", steamAppIdsToNames.size());
	}

	public Mono<UserGameLibraryDto> getUserGameLibrary(Long steamUserId) {
		return webClient.get()
				.uri("https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001?key=" + API_KEY + "&steamid=" + steamUserId + "&format=json")
				.retrieve()
				.onStatus(status -> !status.is2xxSuccessful(),
						clientResponse -> clientResponse.createException().flatMap(Mono::error))
				.bodyToMono(UserGameLibraryDto.class);
	}

	public Game buildGameFromSteamGameId(Long steamGameId) {
		return Game.builder()
				.name(steamAppIdsToNames.getOrDefault((int) (long) steamGameId, "Unknown Game"))
				.steamId(steamGameId)
				.capsuleImageUrl("https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/" + steamGameId + "/capsule_231x87.jpg")
				.libraryImageUrl("https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/" + steamGameId + "/library_600x900.jpg")
				.headerImageUrl("https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/" + steamGameId + "/header.jpg")
				.steamGridDbMissing(false)
				.build();
	}

	public Mono<Long> getSteamFamilyIdForUser(Long steamUserId, String userApiToken) {
		return webClient.get()
				.uri("https://api.steampowered.com/IFamilyGroupsService/GetFamilyGroupForUser/v1/"
						+ "?access_token=" + userApiToken
						+ "&steamid=" + steamUserId
						+ "&include_family_group_response=true")
				.retrieve()
				.onStatus(status -> !status.is2xxSuccessful(),
						clientResponse -> clientResponse.createException().flatMap(Mono::error))
				.bodyToMono(JsonNode.class)
				.map(json -> json.path("response").path("family_groupid").asText(null))
				.filter(id -> id != null && !id.isEmpty())
				.map(Long::valueOf);
	}

	public List<Long> getUserFamilyGameIds(Long familyId, String userApiToken) {
		String uri = "https://api.steampowered.com/IFamilyGroupsService/GetSharedLibraryApps/v1/"
				+ "?access_token=" + userApiToken
				+ "&include_own=true"
				+ "&family_groupid=" + familyId;

		SharedLibraryAppsDto dto = webClient.get()
				.uri(uri)
				.retrieve()
				.onStatus(status -> !status.is2xxSuccessful(),
						clientResponse -> clientResponse.createException().flatMap(Mono::error))
				.bodyToMono(SharedLibraryAppsDto.class)
				.block();

		if (dto == null || dto.getResponse() == null || dto.getResponse().getApps() == null) {
			return Collections.emptyList();
		}

		return dto.getResponse()
				.getApps()
				.stream()
				.map(SharedLibraryAppsDto.App::getAppid)
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList());
	}


}
