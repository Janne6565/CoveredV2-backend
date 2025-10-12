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
}
