package com.clinitalPlatform.payload.response;

import lombok.Data;

@Data
public class ApiResponse {
	private boolean success;
	private String message;
	private Object obj;

	public ApiResponse(boolean success, String message) {
		this.success = success;
		this.message = message;
	}
	public ApiResponse(boolean success, String message,Object obj) {
		this.success = success;
		this.message = message;
		this.obj=obj;
	}

}