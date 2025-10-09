package com.janne.coveredv2.repositories;

import com.janne.coveredv2.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
	User getUserByUuid(String uuid);
}
