package com.clinitalPlatform.security.jwt;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import com.clinitalPlatform.models.User;

import jakarta.persistence.*;

import lombok.Data;

@Entity
@Data
public class ConfirmationToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "token_id")
	private long tokenId;

	@Column(name = "confirmation_token")
	private String confirmationToken;

	@Column(name = "created_date")
	private Date createdDate;

	@Column(name = "expiry_date")
	private Date expiryDate;

	@OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
	@JoinColumn(nullable = false, name = "user_id")
	private User user;

	public ConfirmationToken() {
	}

	public ConfirmationToken(User user) {
		this.user = user;
		createdDate = new Date();
		expiryDate = calculateExpiryDate(10);
		confirmationToken = UUID.randomUUID().toString();
	}

	private Date calculateExpiryDate(int expiryTimeInMinutes) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Timestamp(calendar.getTime().getTime()));
		calendar.add(Calendar.MINUTE, expiryTimeInMinutes);
		return new Date(calendar.getTime().getTime());
	}

}