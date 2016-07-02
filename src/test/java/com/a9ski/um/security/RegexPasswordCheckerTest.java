package com.a9ski.um.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.a9ski.um.config.SystemPropertiesConfigurationProvider;

public class RegexPasswordCheckerTest {

	@Test
	public void testCheckPassword() {
		final RegexPasswordChecker p = createPasswordPolicyChecker();		
		assertFalse(p.checkPassword("", "passwordtest"));
		assertFalse(p.checkPassword("", "passwordTest"));
		assertFalse(p.checkPassword("", "passwordT3st"));		
		assertTrue(p.checkPassword("", "passwordT3st!"));
		assertFalse(p.checkPassword("", "password T3st!"));		
		assertFalse(p.checkPassword("", "t3sT!"));
	}

	private RegexPasswordChecker createPasswordPolicyChecker() {
		final SystemPropertiesConfigurationProvider configProvider = new SystemPropertiesConfigurationProvider();
		final RegexPasswordChecker p = new RegexPasswordChecker(configProvider.getPasswordPolicyCheckerParams());
		return p;
	}
	
	@Test
	public void testCheckPasswordSpecialChars() {
		final String specialChars = "`~!@#$%^&*()-_=+[];:'\"\\|,<.>/?";
		
		final RegexPasswordChecker p = createPasswordPolicyChecker();
		specialChars.chars().forEach( (c) -> { 
			final String pass = "passwordT3st" + (char)c;
			assertTrue(String.format("Expected that password '%s' is passing policy check", pass), p.checkPassword("", pass)); 
		}); 

	}

}
