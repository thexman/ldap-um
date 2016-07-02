package com.a9ski.um.security;

public class DummyPasswordChecker implements PasswordPolicyChecker {

	public DummyPasswordChecker(final Object o) {
		super();
	}
	
	
	@Override
	public boolean checkPassword(String userJson, String password) {
		return true;
	}

}
