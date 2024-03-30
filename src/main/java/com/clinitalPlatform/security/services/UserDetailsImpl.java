package com.clinitalPlatform.security.services;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import com.clinitalPlatform.enums.ERole;
import com.clinitalPlatform.models.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


public class UserDetailsImpl implements UserDetails {
	private Long id;

	private String email;
	
	private String telephone;

	@JsonIgnore
	private String password;

	private ERole role;

	private Boolean isEnabled;
	
    public UserDetailsImpl(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.telephone = user.getTelephone();
        this.password = user.getPassword();
        this.role = user.getRole();
        this.isEnabled = user.isEnabled();
    }
	
	public static UserDetailsImpl build(User user) {

		return new UserDetailsImpl(user);
				
	}
	
	public Long getId() {
		return id;
	}

	
	public ERole getRole() {
		return role;
	}

	public String getEmail() {
		return email;
	}
	
    @Override
    public String getPassword() {
        return password;
    }
   
    @Override
    public String getUsername() {
        return email;
    }

	public String getTelephone() {
		return telephone;
	}

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		UserDetailsImpl user = (UserDetailsImpl) o;
		return Objects.equals(id, user.id);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities(  ) {
		ERole role = this.getRole();
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(role.name()));
		return authorities;
	}
}
