package com.janne.coveredv2.dtos.steamgriddbapi;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GridsDetailDto {
	private boolean success;
	private int page;
	private int total;
	private int limit;
	private GridDto[] data;
}

