package com.janne.coveredv2.dtos;

import com.janne.coveredv2.entities.Game;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameWithPlaytime {
	private long playtime;
	private Game game;
}
