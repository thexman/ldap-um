package com.a9ski.um.ws;

import java.util.Set;

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

import com.a9ski.um.ldap.exceptions.LdapCustomException;
import com.a9ski.um.model.Group;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.util.LDAPSDKException;

@Path("/groups")
public class GroupsWebService extends AbstractWebService {

	private final String groupDnPattern;
	
	public GroupsWebService() {
		super();
		
		final String groupBaseDn = configProvider.getGroupBaseDn();
		
		groupDnPattern = StringUtils.replace(StringUtils.replace(configProvider.getGroupDnPattern(), "<group-base-dn>", groupBaseDn), "<group-id>", "%s");
	}
	
	
	@Path("list")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONArray listAllGroups() throws LDAPException, LdapCustomException {
		final JSONArray j = new JSONArray();
		ldapClient.listAllGroups().forEach(g -> j.put(g.toJSON()));
		return j;
	}
	
	@Path("rows")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject listAllGroupRows() throws LDAPException, LdapCustomException {		
		return new JSONObject().put("rows", listAllGroups());
	}
	
	@Path("create")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject create(@FormParam("group") JSONObject groupJson) { 
		final Group group = Group.fromJSON(groupJson, groupDnPattern);
		try { 
			final Group newGroup = ldapClient.createGroup(group);
			return createSuccessStatus().put("group", newGroup.toJSON());
		} catch (final LDAPSDKException ex) {
			return createFailureStatus(ex);
		}
	}
	
	@Path("searchGroups")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject searchGroups(@QueryParam("search")String search) {
		try {
			final Set<String> groups = ldapClient.searchGroupNames(search);
			return createSuccessStatus().put("groups", new JSONArray(groups));
		} catch (final LDAPException ex) {
			return createFailureStatus(ex);
		}
	}
	
	
	@Path("update")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject update(@FormParam("group") JSONObject groupJson)   { 
		final Group group = Group.fromJSON(groupJson, groupDnPattern);
		try { 
			final Group updatedGroup = ldapClient.updateGroup(group);
			return createSuccessStatus().put("group", updatedGroup.toJSON());
		} catch (final LDAPSDKException ex) {
			return createFailureStatus(ex);
		}
	}
}
