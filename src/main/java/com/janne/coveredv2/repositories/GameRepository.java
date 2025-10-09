package com.janne.coveredv2.repositories;

import com.janne.coveredv2.entities.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, String> {
}
