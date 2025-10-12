package com.janne.coveredv2.dtos.steamgriddbapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GridDto {

    private long id;
    private int score;
    private String style;
    private int width;
    private int height;
    private boolean nsfw;
    private boolean humor;
    private String notes;
    private String mime;
    private String language;
    private String url;
    private String thumb;
    private boolean lock;
    private boolean epilepsy;
    private int upvotes;
    private int downvotes;
    private Author author;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {
        private String name;
        private String steam64;
        private String avatar;
    }
}
