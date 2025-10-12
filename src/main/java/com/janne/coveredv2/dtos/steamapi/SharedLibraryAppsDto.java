package com.janne.coveredv2.dtos.steamapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SharedLibraryAppsDto {

    private Response response;

    @Data
    @NoArgsConstructor
    public static class Response {
        private List<App> apps;
    }

    @Data
    @NoArgsConstructor
    public static class App {
        private Long appid;

        @JsonProperty("owner_steamids")
        private List<String> ownerSteamids;

        private String name;

        @JsonProperty("capsule_filename")
        private String capsuleFilename;

        @JsonProperty("img_icon_hash")
        private String imgIconHash;

        @JsonProperty("exclude_reason")
        private Integer excludeReason;

        @JsonProperty("rt_time_acquired")
        private Long rtTimeAcquired;

        @JsonProperty("rt_last_played")
        private Long rtLastPlayed;

        @JsonProperty("rt_playtime")
        private Long rtPlaytime;

        @JsonProperty("app_type")
        private Integer appType;

        @JsonProperty("content_descriptors")
        private List<Integer> contentDescriptors;
    }
}
