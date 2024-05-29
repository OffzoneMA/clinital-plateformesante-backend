package com.clinitalPlatform.util;

import java.util.List;

import org.springframework.http.HttpStatus;

import lombok.Data;

@Data
public class ApiError extends Throwable {

	private HttpStatus status;
	private String message;
	private List<?> errors;

	public ApiError(HttpStatus status, String message, List<?> errors) {
		super();
		this.status = status;
		this.message = message;
		this.errors = errors;
	}

    public ApiError(boolean b, String string) {
		super();
		this.status = status;
		this.message = message;
    }

	

}