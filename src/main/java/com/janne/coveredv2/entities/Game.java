package com.janne.coveredv2.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Game {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String uuid;
	private String name;
	private Long steamId;
	@Lob
	private String shortDescription;
	private String headerImageUrl;
	private String capsuleImageUrl;
	private String libraryImageUrl;
}
