package com.clinitalPlatform.models;

import com.clinitalPlatform.enums.ERole;
import com.clinitalPlatform.enums.InvitationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "invitations_equipe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationEquipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private ERole role;
    private String token;
    private InvitationStatus status;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "cabinet_id", nullable = false)
    private Cabinet cabinet;

    private LocalDateTime dateEnvoi;
    private LocalDateTime dateExpiration;
    private LocalDateTime dateAccepted;
}
