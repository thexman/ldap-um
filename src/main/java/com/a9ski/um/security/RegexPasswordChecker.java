package com.a9ski.um.security;

import java.util.regex.Pattern;

public class RegexPasswordChecker implements PasswordPolicyChecker {

	private final Pattern passwordPattern;
	
	public RegexPasswordChecker(final String regex) {
		//this.passwordPattern = Pattern.compile(regex, Pattern.DOTALL);
		this.passwordPattern = Pattern.compile(regex);
	}
	
	@Override
	public boolean checkPassword(String userJson, String password) {
		return passwordPattern.matcher(password).matches();
	}

}
