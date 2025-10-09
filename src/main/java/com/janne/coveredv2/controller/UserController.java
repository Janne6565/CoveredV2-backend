package com.janne.coveredv2.controller;

import com.janne.coveredv2.entities.User;
import com.janne.coveredv2.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(BaseController.BASE_URL + "/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping
	public ResponseEntity<User[]> getAllUsers() {
		return ResponseEntity.ok(userService.getAllUsers());
	}

	@PostMapping
	public ResponseEntity<User> addUser(@RequestBody User user) {
		User createdUser = userService.addUser(user);
		return ResponseEntity.ok(createdUser);
	}

	@GetMapping("/{userId}")
	public ResponseEntity<User> getUserById(@PathVariable String userId) {
		return ResponseEntity.ok(userService.getUserById(userId));
	}

	@DeleteMapping("/{userId}")
	public ResponseEntity<String> deleteUserById(@PathVariable String userId) {
		userService.deleteUserById(userId);
		return ResponseEntity.ok("User deleted");
	}
}
