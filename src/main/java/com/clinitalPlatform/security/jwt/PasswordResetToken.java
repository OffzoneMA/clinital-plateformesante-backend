package com.clinitalPlatform.security.jwt;

import com.clinitalPlatform.models.User;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private long tokenId;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "expiry_date")
    private Date expiryDate;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;


    public PasswordResetToken() {
    }

    public PasswordResetToken(User user) {
        this.user = user;
        createdDate = new Date();
        expiryDate = calculateExpiryDate(20); // 10 minutes expiry, adjust as needed
        resetToken = UUID.randomUUID().toString();
    }

    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Timestamp(calendar.getTime().getTime()));
        calendar.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(calendar.getTime().getTime());
    }

    public boolean isExpired() {
        return new Date().after(expiryDate);
    }

}
