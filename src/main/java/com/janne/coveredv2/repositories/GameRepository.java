package com.janne.coveredv2.repositories;

import com.janne.coveredv2.entities.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, String> {
	Optional<Game> findBySteamId(Long steamId);

	boolean existsBySteamId(Long steamId);

	@Query("SELECT g FROM Game g WHERE g.timeOfLastCoverFetch IS NULL")
	List<Game> getGamesWithoutFetchedCovers();
}
