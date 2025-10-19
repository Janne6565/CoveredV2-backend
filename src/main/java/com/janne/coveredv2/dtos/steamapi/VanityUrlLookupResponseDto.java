package com.janne.coveredv2.dtos.steamapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VanityUrlLookupResponseDto {
	private Response response;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Response {
		private int success;
		private String message;
		private String steamid;
	}
}
