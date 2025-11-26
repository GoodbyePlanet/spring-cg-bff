package com.app.auth_server.config.authProviders;

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
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
			log.debug("WebAuthn request found");

			String username = authentication.getName();
			Map<String, Object> body = finishAuthenticationRequestBody(details);

			try {
				String response = webAuthnService.finishAuthentication(body);
				log.debug("WebAuthn authentication successful: {}", response);
				UserDetails user = userDetailsService.loadUserByUsername(username);
				UsernamePasswordAuthenticationToken token = UsernamePasswordAuthenticationToken.authenticated(
					user,
					null,
					user.getAuthorities()
				);

				if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
					token.setDetails(new WebAuthenticationDetails(attributes.getRequest()));
				}

				return token;
			} catch (WebAuthnException e) {
				log.error("WebAuthn validation failed", e);
				throw new BadCredentialsException("Invalid Passkey", e);
			}
		}
		log.debug("No WebAuthn request found, fallback to standard authentication");

		// If no WebAuthn data is present, return null to let DaoAuthenticationProvider
		// handle standard password authentication.
		return null;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

	private static Map<String, Object> finishAuthenticationRequestBody(WebAuthnAuthenticationDetails details) {
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
		return finishBody;
	}

}
