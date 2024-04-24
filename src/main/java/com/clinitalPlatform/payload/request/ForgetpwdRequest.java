package com.clinitalPlatform.payload.request;

import lombok.Data;

import java.util.Date;

@Data
public class ForgetpwdRequest {
    private String patientEmail;
    private Date dateNaissance;
}
