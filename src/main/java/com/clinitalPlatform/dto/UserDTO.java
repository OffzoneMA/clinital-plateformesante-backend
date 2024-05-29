package com.clinitalPlatform.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;


import lombok.Data;

@Data
public class UserDTO {

	private Long id;
	private String email;
	@JsonProperty(access = Access.WRITE_ONLY)
	private String telephone;


}