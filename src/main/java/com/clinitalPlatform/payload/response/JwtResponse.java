package com.clinitalPlatform.payload.response;

import com.clinitalPlatform.enums.ERole;
import com.fasterxml.jackson.annotation.JsonInclude;

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
	//@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int state;

	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int cabinet_docs;

	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private String subPaidStatus;

	public JwtResponse(String accessToken, Long id, String email, String telephone, ERole role,String refreshToken) {
		this.token = accessToken;
		this.id = id;
		this.email = email;
		this.telephone = telephone;
		this.role = role;
		this.refreshToken = refreshToken;
	}
	public JwtResponse(String accessToken, Long id, String email, String telephone, ERole role,String refreshToken,int state) {
		this.token = accessToken;
		this.id = id;
		this.email = email;
		this.telephone = telephone;
		this.role = role;
		this.refreshToken = refreshToken;
		this.state=state;
	}

	public JwtResponse(String accessToken, Long id, String email, String telephone, ERole role,String refreshToken,int state , int  cabinet_docs) {
		this.token = accessToken;
		this.id = id;
		this.email = email;
		this.telephone = telephone;
		this.role = role;
		this.refreshToken = refreshToken;
		this.state=state;
		this.cabinet_docs = cabinet_docs;
	}

	public JwtResponse(String accessToken, Long id, String email, String telephone, ERole role,String refreshToken,int state , int  cabinet_docs , String subPaidStatus) {
		this.token = accessToken;
		this.id = id;
		this.email = email;
		this.telephone = telephone;
		this.role = role;
		this.refreshToken = refreshToken;
		this.state=state;
		this.cabinet_docs = cabinet_docs;
		this.subPaidStatus= subPaidStatus;
	}
}
