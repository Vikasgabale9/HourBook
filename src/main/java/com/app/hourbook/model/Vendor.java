package com.app.hourbook.model;

import com.app.hourbook.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "vendorAuth")
public class Vendor {
	
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	private Integer id;

	@Column(unique = true)
	private String phoneNumber;

	private String otp;

	private boolean isVerified = false;

	private String token;
	
	@Enumerated(EnumType.STRING)
	private Role role;
	
}
