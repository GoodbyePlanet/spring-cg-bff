package com.app.auth_server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeakedPasswordsService {

	private final LeakedPasswordsClient leakedPasswordsClient;

	public boolean isPasswordLeaked(String password) {
		try {
			CheckPasswordResponse response = leakedPasswordsClient.check(new CheckPasswordRequest(password));
			return response.leaked();
		} catch (Exception e) {
			log.error("An error occurred while checking password", e);
			return false;
		}
	}
}
