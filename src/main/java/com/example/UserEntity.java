package com.example;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.LocalDate;

/************************
 * Author: [MR FERRY™]  *
 * July 2025            *
 ************************/

@Builder
@Value
public class UserEntity{
	String email;
	String fullName;
	String password;
	LocalDate birthDate;
	boolean active;
	Instant createdAt;
}
