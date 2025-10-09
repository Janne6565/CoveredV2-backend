package com.janne.coveredv2.service;

import com.janne.coveredv2.entities.Game;
import com.janne.coveredv2.repositories.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameService {

	private final GameRepository gameRepository;

	public void deleteGameById(String gameUuid) {
		gameRepository.deleteById(gameUuid);
	}

	public Game addGame(String name, Long steamId) {
		Game game = gameRepository.save(Game.builder()
				.name(name)
				.steamId(steamId)
				.build());
		return game;
	}
}
