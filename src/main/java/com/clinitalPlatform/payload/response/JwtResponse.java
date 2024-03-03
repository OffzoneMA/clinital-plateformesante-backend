package com.clinitalPlatform.payload.response;


import com.clinital.enums.ERole;
import lombok.Data;

@Data
public class JwtResponse {
	private String token;
	private String type = "Bearer";
	private Long id;
	private String email;
	//private String password;
	private String telephone;
	private ERole role;

	public JwtResponse(String accessToken, Long id, String email, String telephone, ERole role) {
		this.token = accessToken;
		this.id = id;
		this.email = email;
		this.telephone = telephone;
		this.role = role;
	}
	//  public JwtResponse( String email,String password, String telephone) {

	// 	this.password = password;
	// 	this.email = email;
	// 	this.telephone = telephone;
	//  }

	// public String getToken() {
	// 	return this.token;
	// }

}
