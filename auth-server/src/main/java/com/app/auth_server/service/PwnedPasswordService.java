package com.app.auth_server.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class PwnedPasswordService {

	private final RestTemplate restTemplate = new RestTemplate();

	public boolean isPwned(String password) {
		try {
//			ResponseEntity<Boolean> response = restTemplate.postForEntity(
//				"http://pwned-password-service/api/v1/check", password, Boolean.class);
//			return Boolean.TRUE.equals(response.getBody());
			log.info("RETURNING TRUE {}", password);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
