package com.janne.coveredv2.repositories;

import com.janne.coveredv2.entities.Cover;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoverRepository extends JpaRepository<Cover, String> {
	List<Cover> getCoversByGameUuid(String gameUuid);
}
