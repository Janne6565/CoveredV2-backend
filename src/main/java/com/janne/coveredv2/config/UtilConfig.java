package com.janne.coveredv2.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;


@Configuration
public class UtilConfig {

	@Value("${app.response-timeout:1}")
	private int responseTimeout;

	public ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return objectMapper;
	}

	@Bean
	public WebClient webClient() {
		HttpClient httpClient = HttpClient.create()
				.responseTimeout(Duration.ofSeconds(responseTimeout));
		return WebClient.builder()
				.clientConnector(new ReactorClientHttpConnector(httpClient))
				.build();
	}
}
