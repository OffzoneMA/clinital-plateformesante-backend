package com.clinitalPlatform.payload.response;

import com.clinitalPlatform.enums.ERole;

import lombok.Data;

@Data
public class JwtResponse {
	private String token;
	private String refreshToken;

	private String type = "Bearer";
	private Long id;
	private String email;
	private String telephone;
	private ERole role;

	public JwtResponse(String accessToken, Long id, String email, String telephone, ERole role,String refreshToken) {
		this.token = accessToken;
		this.id = id;
		this.email = email;
		this.telephone = telephone;
		this.role = role;
		this.refreshToken = refreshToken;
	}

}
