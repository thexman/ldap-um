/*
 * #%L
 * LDAP User Management
 * %%
 * Copyright (C) 2016 Kiril Arabadzhiyski
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.a9ski.um.config;

import java.io.Serializable;

import com.a9ski.um.ldap.GroupMembershipValue;

public interface ConfigurationProvider extends Serializable {
	public String getUserDnPattern();
	public String getLdapHost();
	public int getLdapPort();
	public String getLdapBindDn();
	public String getLdapPassword();
	public String getUserBaseDn();
	public String getUserSearchFilter();
	public String[] getNewUserObjectClasses();
	public String getGroupBaseDn();
	public String getGroupSearchFilter();
	public String getGroupAttribute();
	public GroupMembershipValue getGroupMembershipValue();
	public String getGroupDnPattern();
	public String[] getNewGroupObjectClasses();
	
}
