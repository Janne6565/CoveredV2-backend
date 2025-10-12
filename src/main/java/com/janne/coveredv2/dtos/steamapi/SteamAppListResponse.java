package com.janne.coveredv2.dtos.steamapi;

import lombok.Data;

import java.util.List;

@Data
public class SteamAppListResponse {
	public AppList applist;

	public static class AppList {
		public List<SteamApp> apps;
	}

	public static class SteamApp {
		public int appid;
		public String name;
	}
}
