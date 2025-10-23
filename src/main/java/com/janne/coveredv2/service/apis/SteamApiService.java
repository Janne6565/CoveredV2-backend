package com.janne.coveredv2.service.apis;

import com.fasterxml.jackson.databind.JsonNode;
import com.janne.coveredv2.dtos.ProfileValidityResponse;
import com.janne.coveredv2.dtos.steamapi.SharedLibraryAppsDto;
import com.janne.coveredv2.dtos.steamapi.UserGameLibraryDto;
import com.janne.coveredv2.dtos.steamapi.VanityUrlLookupResponseDto;
import com.janne.coveredv2.entities.Game;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SteamApiService {

    private final WebClient webClient;
    @Value("${app.steam.api-key}")
    private String API_KEY;

    public Mono<UserGameLibraryDto> getUserGameLibrary(Long steamUserId) {
        return webClient.get()
            .uri("https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001?key=" + API_KEY + "&steamid=" + steamUserId
                + "&format=json&include_appinfo=true&include_played_free_games=true&include_free_sub=true&include_extended_appinfo=true")
            .retrieve()
            .onStatus(status -> !status.is2xxSuccessful(),
                clientResponse -> clientResponse.createException().flatMap(Mono::error))
            .bodyToMono(UserGameLibraryDto.class);
    }

    public Game buildGameFromSteamGameId(Long steamGameId, String gameName, String capsuleFilename) {
        return Game.builder()
            .name(gameName)
            .steamId(steamGameId)
            .capsuleImageUrl("https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/" + steamGameId + "/capsule_231x87.jpg")
            .libraryImageUrl("https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/" + steamGameId + "/library_600x900.jpg")
            .headerImageUrl("https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/" + steamGameId + "/header.jpg")
            .capsuleFilename(capsuleFilename)
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

    public List<SharedLibraryAppsDto.App> getUserFamilyGameIds(Long familyId, String userApiToken) {
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
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
    }

    public String resolveSteamIdFromSteamVanityUrl(String vanityUrl) {
        log.info("Resolving Steam ID for vanity URL {}", vanityUrl);
        VanityUrlLookupResponseDto response = webClient.get()
            .uri("https://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/?key=" + API_KEY + "&vanityurl=" + vanityUrl)
            .retrieve()
            .onStatus(status -> !status.is2xxSuccessful(),
                clientResponse -> clientResponse.createException().flatMap(Mono::error))
            .bodyToMono(VanityUrlLookupResponseDto.class)
            .block();
        if (response.getResponse().getSuccess() != 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vanity URL not found");
        }
        log.info("Resolved Steam ID for vanity URL {} to {}", vanityUrl, response.getResponse().getSteamid());

        return response.getResponse().getSteamid();
    }

    public String getSteamUserName(Long steamId) {
        return webClient.get()
            .uri("https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=" + API_KEY + "&steamids=" + steamId)
            .retrieve()
            .onStatus(status -> !status.is2xxSuccessful(),
                clientResponse -> clientResponse.createException().flatMap(Mono::error))
            .bodyToMono(JsonNode.class)
            .<String>handle((json, sink) -> {
                JsonNode players = json.path("response").path("players");
                if (players.isEmpty()) {
                    sink.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Steam user not found"));
                    return;
                }

                sink.next(players.path(0).path("personaname").asText());
            })
            .block();
    }


    public ProfileValidityResponse isProfileValid(Long steamId) {
        try {
            getSteamUserName(steamId);
        } catch (ResponseStatusException responseStatusException) {
            if (responseStatusException.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return ProfileValidityResponse.builder()
                    .isValid(false)
                    .reason(ProfileValidityResponse.InvalidityReason.PROFILE_NOT_FOUND)
                    .build();
            }
        }
        UserGameLibraryDto gameLibraryDto = getUserGameLibrary(steamId).block();
        if (gameLibraryDto == null || gameLibraryDto.getResponse() == null || gameLibraryDto.getResponse().getGames() == null) {
            return ProfileValidityResponse.builder()
                .isValid(false)
                .reason(ProfileValidityResponse.InvalidityReason.GAMES_NOT_ACCESSIBLE)
                .build();
        }
        return ProfileValidityResponse.builder()
            .isValid(true)
            .build();
    }
}
