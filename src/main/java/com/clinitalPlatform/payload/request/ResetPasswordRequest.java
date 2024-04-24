package com.clinitalPlatform.payload.request;

import lombok.Data;

@Data
public class ResetPasswordRequest {

    private String newPassword;

    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(String newPassword) {
        this.newPassword = newPassword;
    }
}
