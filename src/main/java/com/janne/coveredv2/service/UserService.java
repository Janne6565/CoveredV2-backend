package com.janne.coveredv2.service;

import com.janne.coveredv2.entities.Game;
import com.janne.coveredv2.entities.User;
import com.janne.coveredv2.repositories.GameRepository;
import com.janne.coveredv2.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final SteamApiService steamApiService;
	private final GameRepository gameRepository;

	public User[] getAllUsers() {
		return userRepository.findAll().toArray(new User[0]);
	}

	public User addUser(User user) {
		List<Game> games = List.of(steamApiService.getGamesFromPlayer(user.getSteamId()));
		List<Game> savedGames = gameRepository.saveAll(games);
		user.setGames(savedGames);
		return userRepository.save(user);
	}

	public User getUserById(String userUuid) {
		return userRepository.getUserByUuid(userUuid);
	}

	public void deleteUserById(String userUuid) {
		userRepository.deleteById(userUuid);
	}
}
