package com.janne.coveredv2.dtos.steamgriddbapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties
public class GameDetailDto {
	private boolean success;
	private GameDetailObjectData data;

	@Data
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	@JsonIgnoreProperties
	public static class GameDetailObjectData {
		private long id;
		private String name;
		private long releaseDate;
		private String[] types;
		private boolean verified;
	}
}
