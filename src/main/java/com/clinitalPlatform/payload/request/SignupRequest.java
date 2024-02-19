package com.clinitalPlatform.payload.request;


import com.clinitalPlatform.enums.ERole;

import lombok.Data;

@Data
public class SignupRequest {

	private String email;

	private String telephone;

	private ERole role;

	private String password;

	private boolean conditon;

}
