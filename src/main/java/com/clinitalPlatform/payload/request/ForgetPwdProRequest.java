package com.clinitalPlatform.payload.request;

import lombok.Data;

import java.util.Date;

@Data
public class ForgetPwdProRequest {

    private String proEmail;
    private Date dateNaissance;

}
