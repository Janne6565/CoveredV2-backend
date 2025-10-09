package com.janne.coveredv2.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String uuid;
	private String username;
	private Long steamId;
	@ManyToMany
	private List<Game> games;
}
