package com.janne.coveredv2.dtos.steamapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

@Data
public class UserGameLibraryResponse {

    @JsonProperty("response")
    private Response response;

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Response {
        private int gameCount;
        private List<Game> games;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Game {
        private long appid;
        private int playtimeForever;
        private int playtimeWindowsForever;
        private int playtimeMacForever;
        private int playtimeLinuxForever;
        private int playtimeDeckForever;
        private long rtimeLastPlayed;
        private int playtimeDisconnected;
    }
}
