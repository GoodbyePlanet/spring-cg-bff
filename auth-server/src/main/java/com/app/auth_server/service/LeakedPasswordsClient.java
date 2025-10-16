package com.app.auth_server.service;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

public interface LeakedPasswordsClient {

	@PostExchange(value = "/check", contentType = "application/json")
	CheckPasswordResponse check(@RequestBody CheckPasswordRequest password);
}
