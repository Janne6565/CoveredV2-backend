package com.janne.coveredv2.dtos.steamapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties
public class SteamGameEntity {
	private String type;
	private String name;
	private long steamAppid;
	private int requiredAge;
	private boolean isFree;

	private String detailedDescription;
	private String aboutTheGame;
	private String shortDescription;
	private String supportedLanguages;

	private String headerImage;
	private String capsuleImage;
	private String capsuleImagev5;
	private String website;

	private String legalNotice;
	private List<String> developers;
	private List<String> publishers;

	private PriceOverview priceOverview;
	private List<Long> packages;

	private Platforms platforms;
	private Metacritic metacritic;
	private List<Category> categories;
	private List<Genre> genres;

	private List<Screenshot> screenshots;
	private List<Movie> movies;

	private Recommendations recommendations;
	private Achievements achievements;

	private ReleaseDate releaseDate;
	private SupportInfo supportInfo;

	private String background;
	private String backgroundRaw;

	private ContentDescriptors contentDescriptors;
	private Ratings ratings;

	@Data
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	@JsonIgnoreProperties
	public static class PriceOverview {
		private String currency;
		private int initial;
		@JsonProperty("final")
		private int finalPrice;
		private int discountPercent;
		private String initialFormatted;
		private String finalFormatted;
	}

	@Data
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	@JsonIgnoreProperties
	public static class Platforms {
		private boolean windows;
		private boolean mac;
		private boolean linux;
	}

	@Data
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	@JsonIgnoreProperties
	public static class Metacritic {
		private int score;
		private String url;
	}

	@Data
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	@JsonIgnoreProperties
	public static class Category {
		private int id;
		private String description;
	}

	@Data
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	@JsonIgnoreProperties
	public static class Genre {
		private String id;
		private String description;
	}

	@Data
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	@JsonIgnoreProperties
	public static class Screenshot {
		private int id;
		private String pathThumbnail;
		private String pathFull;
	}

	@Data
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	@JsonIgnoreProperties
	public static class Movie {
		private long id;
		private String name;
		private String thumbnail;
		private VideoUrls webm;
		private VideoUrls mp4;
		private String dashAv1;
		private String dashH264;
		private String hlsH264;
		private boolean highlight;

		@Data
		@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
		@JsonIgnoreProperties
		public static class VideoUrls {
			@JsonProperty("480")
			private String _480;
			private String max;
		}
	}

	@Data
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	@JsonIgnoreProperties
	public static class Recommendations {
		private int total;
	}

	@Data
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	@JsonIgnoreProperties
	public static class Achievements {
		private int total;
		private List<Highlighted> highlighted;

		@Data
		@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
		@JsonIgnoreProperties
		public static class Highlighted {
			private String name;
			private String path;
		}
	}

	@Data
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	@JsonIgnoreProperties
	public static class ReleaseDate {
		private boolean comingSoon;
		private String date;
	}

	@Data
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	@JsonIgnoreProperties
	public static class SupportInfo {
		private String url;
		private String email;
	}

	@Data
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	@JsonIgnoreProperties
	public static class ContentDescriptors {
		private List<Integer> ids;
		private String notes;
	}

	@Data
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	@JsonIgnoreProperties
	public static class Ratings {
		private Agency esrb;
		private Agency pegi;
		private Agency usk;
		private Agency kgrb;
		private Agency bbfc;
		private Agency oflc;
		private Agency nzoflc;
		private Agency cero;
		private Agency dejus;
		private Agency fpb;
		private Agency csrr;
		private Agency crl;

		@Data
		@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
		@JsonIgnoreProperties
		public static class Agency {
			private String rating;
			private String descriptors;
		}
	}
}
