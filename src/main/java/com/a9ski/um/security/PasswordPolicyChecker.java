package com.a9ski.um.security;

public interface PasswordPolicyChecker {
	public boolean checkPassword(String userJson, String password);
}
