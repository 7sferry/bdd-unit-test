package com.example;

/************************
 * Author: [MR FERRY™]  *
 * July 2025            *
 ************************/
public interface UserGateway{
	boolean existsByEmail(String email);
	void save(UserEntity user);
}
