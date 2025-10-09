package com.janne.coveredv2.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(BaseController.BASE_URL)
public class RootController {

	@GetMapping
	public String index() {
		return "Hello World!";
	}
}
