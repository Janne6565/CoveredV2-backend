package com.janne.coveredv2.service.apis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.janne.coveredv2.dtos.steamapi.SteamGameDto;
import com.janne.coveredv2.dtos.steamapi.UserGameLibraryDto;
import com.janne.coveredv2.entities.Game;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
// ... existing code ...

@Slf4j
@Service
@RequiredArgsConstructor
public class SteamApiService {

	private final WebClient webClient = WebClient.create();
	private final ObjectMapper objectMapper;
	@Value("${app.steam.api_key}")
	private String API_KEY;

	public Mono<UserGameLibraryDto> getUserGameLibrary(Long steamUserId) {
		return webClient.get()
				.uri("https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001?key=" + API_KEY + "&steamid=" + steamUserId + "&format=json")
				.retrieve()
				.onStatus(status -> !status.is2xxSuccessful(),
						clientResponse -> clientResponse.createException().flatMap(Mono::error))
				.bodyToMono(UserGameLibraryDto.class);
	}

	public Mono<Game> fetchGameFromSteamId(Long steamId) {
		return webClient.get()
				.uri("https://store.steampowered.com/api/appdetails?appids=" + steamId)
				.retrieve()
				.onStatus(status -> !status.is2xxSuccessful(),
						clientResponse -> clientResponse.createException().flatMap(Mono::error))
				.bodyToMono(JsonNode.class)
				.map(root ->
						root.path(String.valueOf(steamId)).path("data")
				)
				.filter(JsonNode::isObject)
				.map(dataNode -> objectMapper.convertValue(dataNode, SteamGameDto.class))
				.map(this::buildGameFromSteamGameEntity);
	}

	private Game buildGameFromSteamGameEntity(SteamGameDto entity) {
		return Game.builder()
				.name(entity.getName())
				.steamId(entity.getSteamAppid())
				.capsuleImageUrl(entity.getCapsuleImage())
				.headerImageUrl(entity.getHeaderImage())
				.shortDescription(entity.getShortDescription())
				.libraryImageUrl("https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/" + entity.getSteamAppid() + "/library_600x900.jpg")
				.steamGridDbMissing(false)
				.build();
	}
}
