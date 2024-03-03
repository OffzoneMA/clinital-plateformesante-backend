package com.clinitalPlatform.payload.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginRequest {
	@NotBlank(message = "{validation.mail.notEmpty}")
	private String email;

	@NotBlank(message = "{validation.password.notEmpty}")
	private String password;

}
