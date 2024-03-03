package com.clinitalPlatform.payload.response;

import com.clinital.dto.UserDTO;
import lombok.Data;

import java.util.Date;

@Data
public class activityLogResponse {
        
        private Long id;
        private Date TimeActivity;
        private String typeActivity;
        private String description;
        private UserDTO user;
        
    
}
