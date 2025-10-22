package com.janne.coveredv2.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SteamIdResponse {
	private String steamid;
}
