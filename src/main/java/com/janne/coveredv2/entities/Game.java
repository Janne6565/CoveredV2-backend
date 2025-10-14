package com.janne.coveredv2.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Game {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String uuid;
	private String name;
	private Long steamId;
	@Lob
	private String headerImageUrl;
	private String capsuleImageUrl;
	private String libraryImageUrl;
	private Long timeOfLastCoverFetch;
	private Boolean steamGridDbMissing;
	private String capsuleFilename;
}
