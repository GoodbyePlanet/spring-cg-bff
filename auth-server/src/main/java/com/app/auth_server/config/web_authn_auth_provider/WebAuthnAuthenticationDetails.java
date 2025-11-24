package com.app.auth_server.config.web_authn_auth_provider;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import java.util.Objects;

@Getter
public class WebAuthnAuthenticationDetails extends WebAuthenticationDetails {

	private final String id;
	private final String type;
	private final String authenticatorAttachment;
	private final String rawId;
	private final String authenticatorData;
	private final String clientDataJSON;
	private final String signature;
	private final String userHandle;

	public WebAuthnAuthenticationDetails(HttpServletRequest request) {
		super(request);
		this.id = request.getParameter("id");
		this.type = request.getParameter("type");
		this.authenticatorAttachment = request.getParameter("authenticatorAttachment");
		this.rawId = request.getParameter("rawId");
		this.authenticatorData = request.getParameter("authenticatorData");
		this.clientDataJSON = request.getParameter("clientDataJSON");
		this.signature = request.getParameter("signature");
		this.userHandle = request.getParameter("userHandle");
	}

	public boolean isWebAuthnRequest() {
		return Objects.nonNull(id) && !id.isBlank()
			&& Objects.nonNull(rawId) && !rawId.isBlank()
			&& Objects.nonNull(authenticatorData) && !authenticatorData.isBlank()
			&& Objects.nonNull(clientDataJSON) && !clientDataJSON.isBlank()
			&& Objects.nonNull(signature) && !signature.isBlank();
	}
}
