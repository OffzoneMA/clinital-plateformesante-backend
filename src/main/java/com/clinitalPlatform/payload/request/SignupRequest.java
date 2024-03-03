package com.clinitalPlatform.payload.request;

import com.clinital.enums.ERole;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class SignupRequest {

	@NotBlank
	@Size(max = 50)
	@Email
	private String email;

	@NotNull
	private String telephone;

	private ERole role;

	@NotBlank
	@Size(min = 6, max = 40)
	private String password;

	private boolean conditon;

}
