package com.example;

import lombok.RequiredArgsConstructor;

import java.time.Instant;

/************************
 * Author: [MR FERRY™]  *
 * July 2025            *
 ************************/

@RequiredArgsConstructor
public class UserRegistrationUseCase{
	private final UserGateway userGateway;
	private final NotificationClient notificationClient;

	public void registerUser(UserRequest userRequest, UserRegistrationPresenter presenter){
		if(userRequest.getEmail() == null || userRequest.getEmail().trim().isEmpty()){
			throw new IllegalArgumentException("Invalid email");
		}
		if(userRequest.getPassword() == null || userRequest.getPassword().trim().isEmpty() || userRequest.getPassword().length() < 8){
			throw new IllegalArgumentException("Invalid password");
		}
		if(userGateway.existsByEmail(userRequest.getEmail())){
			throw new IllegalArgumentException("Email already exists");
		}

		UserEntity user = UserEntity.builder()
				.email(userRequest.getEmail())
				.fullName(userRequest.getFullName())
				.password(userRequest.getPassword())
				.birthDate(userRequest.getBirthDate())
				.active(true)
				.createdAt(Instant.now())
				.build();
		userGateway.save(user);

		notificationClient.sendWelcomeEmail(new EmailRequest(user.getEmail(), user.getFullName()));

		presenter.present(new UserRegistrationResponse(user.getEmail(), "User registered successfully"));
	}

}

