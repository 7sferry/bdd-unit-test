package com.example;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.BDDSoftAssertions.thenSoftly;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegistrationUseCaseTest{
	public static final String EMAIL = "peter@parker.com";
	public static final String FULL_NAME = "peter";
	public static final String PASSWORD = "abc12345678";
	public static final LocalDate BIRTH_DATE = LocalDate.of(1919, 1, 1);

	@Mock
	UserGateway userRepository;

	@Mock
	NotificationClient notificationClient;

	@Mock
	UserRegistrationPresenter userRegistrationPresenter;

	@InjectMocks
	UserRegistrationUseCase userService;

	@Captor
	ArgumentCaptor<UserEntity> userEntityCaptor;

	@Captor
	ArgumentCaptor<UserRegistrationResponse> responseCaptor;

	@Test
	void givenValidRequest_thenRegisterUserSuccessfully() {
		// given
		UserRequest userRequest = UserRequest.builder()
				.email(EMAIL)
				.fullName(FULL_NAME)
				.password(PASSWORD)
				.birthDate(BIRTH_DATE)
				.build();
		willReturn(false).given(userRepository).existsByEmail(anyString());

		// when
		userService.registerUser(userRequest, userRegistrationPresenter);

		// then
		then(userRepository).should(times(1)).save(userEntityCaptor.capture());
		then(notificationClient).should(times(1)).sendWelcomeEmail(any(EmailRequest.class));
		then(userRegistrationPresenter).should(times(1)).present(responseCaptor.capture());
		thenSoftly(and -> {
			UserEntity userEntity = userEntityCaptor.getValue();
			and.then(userEntity.getEmail()).isEqualTo(EMAIL);
			and.then(userEntity.getFullName()).isEqualTo(userRequest.getFullName());
			and.then(userEntity.getBirthDate()).isEqualTo(userRequest.getBirthDate());
			and.then(userEntity.getCreatedAt()).isCloseTo(Instant.now(), Assertions.within(Duration.ofHours(1)));
			and.then(userEntity.isActive()).isTrue();

			UserRegistrationResponse result = responseCaptor.getValue();
			and.then(result).isNotNull();
			and.then(result.getEmail()).isEqualTo(EMAIL);
			and.then(result.getMessage()).isEqualTo("User registered successfully");

		});
	}

	@Test
	void givenExistsEmail_thenThrowException() {
		// given
		UserRequest userRequest = UserRequest.builder()
				.email(EMAIL)
				.fullName(FULL_NAME)
				.password(PASSWORD)
				.birthDate(BIRTH_DATE)
				.build();
		willReturn(true).given(userRepository).existsByEmail(EMAIL);

		//when
		thenSoftly(and -> {
			and.thenExceptionOfType(IllegalArgumentException.class)
					.isThrownBy(() -> userService.registerUser(userRequest, userRegistrationPresenter))
					.withMessageContaining("Email already exists");
		});

		//then
		then(userRepository).should(times(1)).existsByEmail(anyString());
		then(userRepository).should(never()).save(any());
		then(notificationClient).shouldHaveNoInteractions();
		then(userRegistrationPresenter).shouldHaveNoInteractions();
	}

	@ParameterizedTest
	@NullAndEmptySource
	void givenInvalidEmail_thenThrowException(String email) {
		// given
		UserRequest userRequest = UserRequest.builder()
				.email(email)
				.fullName(FULL_NAME)
				.password(PASSWORD)
				.birthDate(BIRTH_DATE)
				.build();

		//when
		thenSoftly(and -> {
			and.thenExceptionOfType(IllegalArgumentException.class)
					.isThrownBy(() -> userService.registerUser(userRequest, userRegistrationPresenter))
					.withMessageContaining("Invalid email");
		});

		//then
		then(userRepository).shouldHaveNoInteractions();
		then(notificationClient).shouldHaveNoInteractions();
		then(userRegistrationPresenter).shouldHaveNoInteractions();
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"abc123", "abcdefghijk", "1234567890"})
	void givenInvalidPassword_thenThrowException(String password) {
		// given
		UserRequest userRequest = UserRequest.builder()
				.email(EMAIL)
				.fullName(FULL_NAME)
				.password(password)
				.birthDate(BIRTH_DATE)
				.build();

		//when
		thenSoftly(and -> {
			and.thenExceptionOfType(IllegalArgumentException.class)
					.isThrownBy(() -> userService.registerUser(userRequest, userRegistrationPresenter))
					.withMessageContaining("Invalid password");
		});

		//then
		then(userRepository).shouldHaveNoInteractions();
		then(notificationClient).shouldHaveNoInteractions();
		then(userRegistrationPresenter).shouldHaveNoInteractions();
	}

}
