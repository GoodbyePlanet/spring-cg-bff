package com.app.auth_server.webauthn;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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

		ResponseEntity<String> downstreamResponse = webAuthnService.beginAuthentication(username);
		ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(HttpStatus.OK);
		List<String> cookies = downstreamResponse.getHeaders().get(HttpHeaders.SET_COOKIE);

		if (cookies != null) {
			for (String cookie : cookies) {
				responseBuilder.header(HttpHeaders.SET_COOKIE, cookie);
			}
		}

		return responseBuilder.body(downstreamResponse.getBody());
	}
}
