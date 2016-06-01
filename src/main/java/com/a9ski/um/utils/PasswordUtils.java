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
package com.a9ski.um.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordUtils {
	private PasswordUtils() {
		super();
	}
	
	public static String encryptPassword(String pass) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		final MessageDigest md = MessageDigest.getInstance("SHA");
		md.update(pass.getBytes("UTF-8"));
		final String data = Base64.getEncoder().encodeToString(md.digest());
		return "{sha}" + data;		
	}
}
