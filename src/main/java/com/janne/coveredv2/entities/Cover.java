package com.janne.coveredv2.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.janne.coveredv2.dtos.steamgriddbapi.GridDto;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Cover {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String uuid;
	private long steamGridDbId;
	private String style;
	private String gameUuid;
	private int width;
	private int height;
	private boolean nsfw;
	private boolean humor;
	@Lob
	private String notes;
	private String mime;
	private String language;
	private String thumb;
	private String url;
	@Embedded
	private Author author;

	@Builder
	@Embeddable
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Author {
		private String name;
		private String steam64;
		private String avatar;

		public static Author convertFromSteamGridDbApi(GridDto.Author author) {
			return Author.builder()
					.name(author.getName())
					.steam64(author.getSteam64())
					.avatar(author.getAvatar())
					.build();
		}
	}
}
