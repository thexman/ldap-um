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
