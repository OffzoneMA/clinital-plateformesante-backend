package com.clinitalPlatform.payload.request;

import com.clinitalPlatform.enums.ERole;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationEquipeRequest {

    private String email;
    private ERole role;
}
