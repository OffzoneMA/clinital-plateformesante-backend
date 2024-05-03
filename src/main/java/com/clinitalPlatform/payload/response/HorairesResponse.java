package com.clinitalPlatform.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorairesResponse {

	private String startTime;
	private String endTime;

}
