package com.app.auth_server.config.web_authn_auth_provider;

import com.app.auth_server.webauthn.WebAuthnException;
import com.app.auth_server.webauthn.WebAuthnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebAuthnAuthenticationProvider implements AuthenticationProvider {

	private final WebAuthnService webAuthnService;
	private final UserDetailsService userDetailsService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		log.info("Attempting WebAuthn authentication");
		if (authentication.getDetails() instanceof WebAuthnAuthenticationDetails details
			&& details.isWebAuthnRequest()) {

			log.info("WebAuthn request found");
			String username = authentication.getName();
			log.debug("Attempting WebAuthn authentication for user: {}", username);

			Map<String, Object> responseMap = new HashMap<>();
			responseMap.put("authenticatorData", details.getAuthenticatorData());
			responseMap.put("clientDataJSON", details.getClientDataJSON());
			responseMap.put("signature", details.getSignature());
			String userHandle = details.getUserHandle();
			responseMap.put("userHandle", (userHandle != null && !userHandle.isEmpty()) ? userHandle : null);

			Map<String, Object> finishBody = new HashMap<>();
			finishBody.put("id", details.getId());
			finishBody.put("rawId", details.getRawId());
			finishBody.put("type", details.getType());
			finishBody.put("authenticatorAttachment", details.getAuthenticatorAttachment());
			finishBody.put("response", responseMap);

			try {
				String response = webAuthnService.finishAuthentication(finishBody);
				log.debug("WebAuthn authentication successful: {}", response);
				UserDetails user = userDetailsService.loadUserByUsername(username);

				return UsernamePasswordAuthenticationToken.authenticated(
					user,
					null,
					user.getAuthorities()
				);

			} catch (WebAuthnException e) {
				log.error("WebAuthn validation failed", e);
				throw new BadCredentialsException("Invalid Passkey", e);
			}
		}

		// If no WebAuthn data is present, return null to let DaoAuthenticationProvider
		// handle standard password checking.
		return null;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
