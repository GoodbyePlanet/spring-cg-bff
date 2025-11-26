package com.app.auth_server.webauthn;

public class WebAuthnException extends RuntimeException {
	public WebAuthnException(String message, Throwable cause) {
		super(message, cause);
	}
}
