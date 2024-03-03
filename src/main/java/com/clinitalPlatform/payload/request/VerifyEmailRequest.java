package com.clinitalPlatform.payload.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class VerifyEmailRequest {

	@NotBlank
	@Email
	private String email;

	private Integer otpNo;

}