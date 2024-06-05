package com.clinitalPlatform.dto;

import java.util.Date;

import lombok.Data;

@Data
public class ActivityDTO {

    private Long id;
    
    private Date TimeActivity;
    
    private String typeActivity;
    
    private String description;
    
    private UserDTO user;



    
}
