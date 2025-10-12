package com.janne.coveredv2.repositories;

import com.janne.coveredv2.entities.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GameRepository extends JpaRepository<Game, String> {
	Optional<Game> findBySteamId(Long steamId);

	@Query("SELECT g FROM Game g WHERE g.timeOfLastCoverFetch IS NULL")
	List<Game> getGamesWithoutFetchedCovers();

	Page<Game> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
