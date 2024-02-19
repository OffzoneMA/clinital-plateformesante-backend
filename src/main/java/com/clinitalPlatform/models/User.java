package com.clinitalPlatform.models;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

import com.clinitalPlatform.enums.ProviderEnum;
import com.clinitalPlatform.enums.ERole;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import lombok.Data;

@Table(name = "users")
@Data
@Entity
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String email;
	private String telephone;
	
	@JsonIgnore
	private String password;
	private boolean isEnabled = false;
	
	@Column(nullable = false)
	private Boolean emailVerified = false;
	
	@Enumerated(EnumType.STRING)
	private ERole role;
	
	@Enumerated(EnumType.STRING)
	private ProviderEnum provider;
	
	@CreationTimestamp
	@Column(name = "creation_date_time")
	private Date creationDateTime;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_login")
	private Date lastLogin;
	
	public User() {
		super();
	}
	public User( String email, String telephone,
			 String password) {
		super();
		this.email = email;
		this.telephone = telephone;
		this.password = password;
	}

	public User( String email,  String telephone,
			 String password, ERole role) {
		super();
		this.email = email;
		this.telephone = telephone;
		this.password = password;
		this.role = role;
	}
}

