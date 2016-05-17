package com.a9ski.um.ws;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.a9ski.um.config.ConfigurationProvider;
import com.a9ski.um.config.ConfigurationProviderFactory;
import com.a9ski.um.ldap.LdapClient;
import com.a9ski.um.model.User;
import com.unboundid.util.LDAPSDKException;

public abstract class AbstractWebService {

	protected final Logger logger = LoggerFactory.getLogger(AbstractWebService.class);
	protected final ConfigurationProvider configProvider;
	protected final LdapClient ldapClient;
	
	@Context 
	protected SecurityContext securityContext;
	
	@Context 
	protected UriInfo uri;
	
	@Context
    protected HttpServletRequest request;

	protected AbstractWebService() {
		super();
		
		configProvider = createConfigurationProvider();
		
		ldapClient = createLdapClient();

	}

	protected LdapClient createLdapClient() {
		final LdapClientFactory ldapClientFactory = new LdapClientFactory(configProvider);		
		return ldapClientFactory.createLdapClient();
	}

	protected ConfigurationProvider createConfigurationProvider() {
		return ConfigurationProviderFactory.createFactory().createConfigurationProvider();
	}

	@Path("test")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String test() {
		return "It works!";
	}

	@Path("json")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject json() {
		final JSONObject j = new JSONObject("{ 'status' : 'It works!' }");
		return j;
	}

	private JSONObject createStatus(String status) {
		return new JSONObject().put("status", status);
	}

	protected JSONObject createSuccessStatus() {
		return createStatus("success");
	}

	private JSONObject createFailureStatus() {
		return createStatus("failure");
	}

	protected JSONObject createFailureStatus(Exception ex) {
		logger.error("Failure status", ex);
		return createFailureStatus(ex.getMessage()).put("exception", true);
	}

	protected JSONObject createFailureStatus(String message) {
		return createFailureStatus().put("message", message);
	}
	
	protected User getCurrentUser() throws LDAPSDKException {
		final Principal p = securityContext.getUserPrincipal();
		final User u;
		if (p != null) {
			u = ldapClient.findUserByUid(p.getName());
		} else {
			u = null;
		}
		return u;
	}
	
	protected HttpSession getSession() {
		return request.getSession();
	}
	
	protected void assertAdmin() {
		if (!securityContext.isUserInRole("UmAdmins")) {
			throw new SecurityException("Administrator role required");
		}
	}

}