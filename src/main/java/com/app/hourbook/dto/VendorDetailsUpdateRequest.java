package com.app.hourbook.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorDetailsUpdateRequest {
	

	@NotBlank(message = "Password is required")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&^#()_+=\\-])[A-Za-z\\d@$!%*?&^#()_+=\\-]{8,20}$", message = "Password must be 8-20 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character.")
	private String password;

	@Email
	@NotBlank(message = "email is required")
	private String email;
	
	@NotBlank(message = "fullname is required")
	@Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters.")
	@Pattern(
	    regexp = "^[A-Za-z]+(?:[\\s'-][A-Za-z]+)*$", 
	    message = "Full name can only contain letters, spaces, hyphens, and apostrophes."
	)
	private String fullname;
	
}