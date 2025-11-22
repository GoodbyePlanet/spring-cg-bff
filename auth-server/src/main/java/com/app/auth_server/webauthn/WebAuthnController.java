package com.app.auth_server.webauthn;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/webauthn")
public class WebAuthnController {

	private final WebAuthnService webAuthnService;

	@PostMapping("/begin")
	public ResponseEntity<?> begin(@RequestBody Map<String, String> body) {
		String username = body.get("username");
		log.info("Passkey begin request for user with {} username", username);

		String options = webAuthnService.beginAuthentication(username);
		return ResponseEntity.status(HttpStatus.OK).body(options);
	}
}
