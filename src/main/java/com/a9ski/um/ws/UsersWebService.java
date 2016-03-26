package com.a9ski.um.ws;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.a9ski.um.config.ConfigurationProvider;
import com.a9ski.um.config.ConfigurationProviderFactory;
import com.a9ski.um.ldap.LdapClient;
import com.a9ski.um.model.User;
import com.a9ski.um.utils.PasswordUtils;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.util.LDAPSDKException;

@Path("/users")
public class UsersWebService {

	private final ConfigurationProvider configProvider = ConfigurationProviderFactory.createConfigurationProvider();
	private final String dnPattern;	
	private final LdapClient ldapClient;
	
	public UsersWebService() {
		super();
		
		final String host = configProvider.getLdapHost();
		final int port = configProvider.getLdapPort();
		final String bindDn = configProvider.getLdapBindDn();
		final String password = configProvider.getLdapPassword();
		final String userBaseDn = configProvider.getUserBaseDn();
		
		ldapClient = new LdapClient(host, port, bindDn, password, userBaseDn);
		dnPattern = StringUtils.replace(StringUtils.replace(configProvider.getUserDnPattern(), "<user-base-dn>", userBaseDn), "<user-id>", "%s");
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
	
	@Path("list")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONArray listAllUsers() throws LDAPException {
		final JSONArray j = new JSONArray();
		for(final User u : ldapClient.listAllUsers()) {
			j.put(u.toJSON());
		}
		return j;
	}
	
	@Path("rows")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject listAllUserRows() throws LDAPException {		
		return new JSONObject().put("rows", listAllUsers());
	}
	
	@Path("create")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject create(@FormParam("user") JSONObject userJson) { 
		System.out.println(userJson);		
		return createSuccessSatus().put("user", userJson);
	}
	
	@Path("update")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject update(@FormParam("user") JSONObject userJson) { 
		System.out.println(userJson);		
		return createSuccessSatus().put("user", userJson);
	}
	
	@Path("password")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject setPassword(@QueryParam("uid")String uid, @QueryParam("newPassword")String newPassword, @QueryParam("oldPassword")String oldPassword) throws UnsupportedEncodingException, NoSuchAlgorithmException, LDAPSDKException {		
		if (ldapClient.checkPassword(String.format(dnPattern, LdapClient.escapeDnLiteral(uid)), oldPassword)) {
			ldapClient.changePassword(uid, PasswordUtils.encryptPassword(newPassword));
			return createSuccessSatus();
		} else {
			return createFailureSatus();
		}
		
	}		
	
	private JSONObject createStatus(String status) {
		return new JSONObject().put("status", status);
	}
	
	private JSONObject createSuccessSatus() {
		return createStatus("success");
	}
	
	private JSONObject createFailureSatus() {
		return createStatus("failure");
	}
	
}
