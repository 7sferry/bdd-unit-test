package com.example;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

/************************
 * Author: [MR FERRY™]  *
 * July 2025            *
 ************************/

@Builder
@Value
public class UserRequest{
	String email;
	String fullName;
	String password;
	LocalDate birthDate;
}
